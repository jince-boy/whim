package com.whim.redis.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RTopic;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Callable;

/**
 * @author Jince
 * @date 2026/03/30
 * @description 二级缓存装饰器（Caffeine L1 + Redis L2）。
 * <p>
 * 读取时优先查询本地 Caffeine 缓存，未命中再回源 Redis。
 * 写入/失效时同步清除本地缓存，并通过 Redisson RTopic 广播失效消息，
 * 实现多节点本地缓存的最终一致性。
 */
@Slf4j
public class CaffeineCacheDecorator implements Cache {

    private static final String INVALIDATION_TOPIC = "cache:invalidation";
    private static final String SEPARATOR = "::";
    private static final String KEY_SIGNATURE_SEPARATOR = "|";
    private static final String WILDCARD = "*";
    private static final Object NULL_HOLDER = new Object();
    private static final Field SIMPLE_KEY_PARAMS_FIELD = resolveSimpleKeyParamsField();

    private final String name;
    private final Cache delegate;

    @Getter
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache;
    private final RTopic invalidationTopic;
    private final String nodeId;
    private final JsonMapper jsonMapper;

    /**
     * 创建二级缓存装饰器
     *
     * @param name             缓存名称
     * @param delegate         底层 Redis 缓存实现
     * @param localCache       当前缓存专属的 Caffeine 本地缓存实例
     * @param invalidationTopic Redisson 缓存失效广播主题
     * @param nodeId           当前节点唯一标识，用于过滤自身广播
     * @param jsonMapper       用于生成稳定本地缓存键的 JsonMapper
     */
    public CaffeineCacheDecorator(String name,
                                  Cache delegate,
                                  com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache,
                                  RTopic invalidationTopic,
                                  String nodeId,
                                  JsonMapper jsonMapper) {
        this.name = name;
        this.delegate = delegate;
        this.localCache = localCache;
        this.invalidationTopic = invalidationTopic;
        this.nodeId = nodeId;
        this.jsonMapper = jsonMapper;
    }

    /**
     * 获取缓存失效广播主题名称
     *
     * @return 主题名称
     */
    public static String getInvalidationTopicName() {
        return INVALIDATION_TOPIC;
    }

    /**
     * 构建本地缓存使用的稳定键，确保跨节点失效广播能够正确命中。
     *
     * @param key 原始缓存键
     * @return 本地缓存键
     */
    private String buildLocalCacheKey(Object key) {
        if (key == null) {
            return buildTypedSignature(Void.class, "null");
        }
        if (key instanceof SimpleKey simpleKey) {
            return buildTypedSignature(SimpleKey.class, serializeSimpleKey(simpleKey));
        }
        if (key.getClass().isArray()) {
            return buildTypedSignature(key.getClass(), serializeArrayKey(key));
        }
        if (isSimpleKeyType(key)) {
            return buildTypedSignature(key.getClass(), normalizeSimpleKeyValue(key));
        }
        return buildTypedSignature(key.getClass(), serializeObjectKey(key));
    }

    /**
     * Return the cache name.
     */
    @Override
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Return the underlying native cache provider.
     */
    @Override
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Object cachedValue = getCachedValue(key);
        if (cachedValue == null) {
            return null;
        }
        return () -> cachedValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        Object cachedValue = getCachedValue(key);
        if (cachedValue == null) {
            return null;
        }
        if (type != null && !type.isInstance(cachedValue)) {
            throw new IllegalStateException("缓存值类型不匹配，缓存名称：%s，期望类型：%s，实际类型：%s"
                    .formatted(name, type.getName(), cachedValue.getClass().getName()));
        }
        return (T) cachedValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        String localCacheKey = buildLocalCacheKey(key);
        Object cachedValue = getLocalCachedValue(localCacheKey);
        if (cachedValue != null || localCache.asMap().containsKey(localCacheKey)) {
            return (T) cachedValue;
        }
        T loadedValue = delegate.get(key, valueLoader);
        localCache.put(localCacheKey, toStoreValue(loadedValue));
        return loadedValue;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        String localCacheKey = buildLocalCacheKey(key);
        delegate.put(key, value);
        localCache.put(localCacheKey, toStoreValue(value));
        publishInvalidation(localCacheKey);
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        ValueWrapper wrapper = delegate.putIfAbsent(key, value);
        String localCacheKey = buildLocalCacheKey(key);
        if (wrapper == null) {
            localCache.put(localCacheKey, toStoreValue(value));
            publishInvalidation(localCacheKey);
        } else {
            localCache.put(localCacheKey, toStoreValue(wrapper.get()));
        }
        return wrapper;
    }

    @Override
    public void evict(@NonNull Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(@NonNull Object key) {
        String localCacheKey = buildLocalCacheKey(key);
        boolean evicted = delegate.evictIfPresent(key);
        if (evicted) {
            localCache.invalidate(localCacheKey);
            publishInvalidation(localCacheKey);
        }
        return evicted;
    }

    @Override
    public void clear() {
        localCache.invalidateAll();
        delegate.clear();
        publishInvalidation(WILDCARD);
    }

    @Override
    public boolean invalidate() {
        boolean result = delegate.invalidate();
        localCache.invalidateAll();
        publishInvalidation(WILDCARD);
        return result;
    }

    /**
     * 删除本地缓存中的指定键。
     *
     * @param localCacheKey 本地缓存稳定键
     */
    public void invalidateLocalEntry(String localCacheKey) {
        localCache.invalidate(localCacheKey);
    }

    /**
     * 清空当前缓存对应的全部本地缓存。
     */
    public void invalidateLocalAll() {
        localCache.invalidateAll();
    }

    /**
     * 广播缓存失效消息。格式：nodeId::cacheName::localCacheKey
     *
     * @param localCacheKey 本地缓存稳定键，使用 "*" 表示清空整个缓存
     */
    private void publishInvalidation(String localCacheKey) {
        try {
            String message = nodeId + SEPARATOR + name + SEPARATOR + localCacheKey;
            invalidationTopic.publish(message);
        } catch (Exception e) {
            log.warn("缓存失效广播发送失败，缓存名称：{}，本地缓存键：{}", name, localCacheKey, e);
        }
    }

    /**
     * 获取缓存值，优先读取本地缓存，未命中时再回源 Redis。
     *
     * @param key 缓存键
     * @return 缓存值，未命中时返回 null
     */
    private Object getCachedValue(Object key) {
        String localCacheKey = buildLocalCacheKey(key);
        Object cachedValue = getLocalCachedValue(localCacheKey);
        if (cachedValue != null || localCache.asMap().containsKey(localCacheKey)) {
            return cachedValue;
        }
        ValueWrapper wrapper = delegate.get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        localCache.put(localCacheKey, toStoreValue(value));
        return value;
    }

    /**
     * 读取本地缓存中的值，并处理空值占位符。
     *
     * @param localCacheKey 本地缓存键
     * @return 实际缓存值
     */
    private Object getLocalCachedValue(String localCacheKey) {
        Object localValue = localCache.getIfPresent(localCacheKey);
        return fromStoreValue(localValue);
    }

    /**
     * 将缓存值转换为本地缓存可存储的形式。
     *
     * @param value 原始缓存值
     * @return 本地缓存存储值
     */
    private Object toStoreValue(Object value) {
        return value == null ? NULL_HOLDER : value;
    }

    /**
     * 将本地缓存中的存储值还原为业务值。
     *
     * @param storeValue 本地缓存存储值
     * @return 业务值
     */
    private Object fromStoreValue(Object storeValue) {
        return storeValue == NULL_HOLDER ? null : storeValue;
    }

    /**
     * 判断是否为可直接规范化为文本的简单缓存键类型。
     *
     * @param key 缓存键
     * @return 是否为简单键类型
     */
    private boolean isSimpleKeyType(Object key) {
        return key instanceof CharSequence
                || key instanceof Number
                || key instanceof Boolean
                || key instanceof Character
                || key instanceof Enum<?>
                || key instanceof java.util.UUID
                || key instanceof Class<?>;
    }

    /**
     * 将简单缓存键转换为稳定文本，避免不同类型但字符串值相同的键发生碰撞。
     *
     * @param key 缓存键
     * @return 稳定文本
     */
    private String normalizeSimpleKeyValue(Object key) {
        if (key instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        if (key instanceof Class<?> type) {
            return type.getName();
        }
        return String.valueOf(key);
    }

    /**
     * 将 SimpleKey 转换为稳定文本，确保多参数缓存键跨节点保持一致。
     *
     * @param simpleKey Spring Cache 默认复合键
     * @return 稳定文本
     */
    private String serializeSimpleKey(SimpleKey simpleKey) {
        Object[] params = extractSimpleKeyParams(simpleKey);
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < params.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(buildLocalCacheKey(params[index]));
        }
        return builder.toString();
    }

    /**
     * 将数组缓存键转换为稳定文本，避免数组 toString 产生内存地址语义。
     *
     * @param arrayKey 数组缓存键
     * @return 稳定文本
     */
    private String serializeArrayKey(Object arrayKey) {
        int length = Array.getLength(arrayKey);
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(buildLocalCacheKey(Array.get(arrayKey, index)));
        }
        return builder.toString();
    }

    /**
     * 将复杂对象缓存键序列化为稳定字节，作为本地缓存与失效广播的统一标识。
     *
     * @param key 复杂对象缓存键
     * @return 稳定字节
     */
    private byte[] serializeObjectKey(Object key) {
        try {
            return jsonMapper.writeValueAsBytes(key);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "缓存[%s]的键类型[%s]无法生成稳定签名，请改用简单类型键或可稳定序列化的对象键。"
                            .formatted(name, key.getClass().getName()),
                    exception);
        }
    }

    /**
     * 基于键类型与规范文本生成稳定签名。
     *
     * @param keyType 键类型
     * @param canonicalValue 规范文本
     * @return 稳定签名
     */
    private String buildTypedSignature(Class<?> keyType, String canonicalValue) {
        return buildTypedSignature(keyType, canonicalValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 基于键类型与规范字节生成稳定签名。
     *
     * @param keyType 键类型
     * @param canonicalBytes 规范字节
     * @return 稳定签名
     */
    private String buildTypedSignature(Class<?> keyType, byte[] canonicalBytes) {
        return keyType.getName()
                + KEY_SIGNATURE_SEPARATOR
                + Base64.getUrlEncoder().withoutPadding().encodeToString(canonicalBytes);
    }

    /**
     * 提取 Spring SimpleKey 内部维护的参数数组。
     *
     * @param simpleKey Spring Cache 默认复合键
     * @return 参数数组
     */
    private Object[] extractSimpleKeyParams(SimpleKey simpleKey) {
        try {
            return (Object[]) SIMPLE_KEY_PARAMS_FIELD.get(simpleKey);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取 Spring SimpleKey 参数失败，无法生成稳定本地缓存键。", exception);
        }
    }

    /**
     * 解析 Spring SimpleKey 的参数字段，供多参数缓存键生成稳定签名。
     *
     * @return SimpleKey 参数字段
     */
    private static Field resolveSimpleKeyParamsField() {
        try {
            Field field = SimpleKey.class.getDeclaredField("params");
            field.setAccessible(true);
            return field;
        } catch (Exception exception) {
            throw new IllegalStateException("初始化 Spring SimpleKey 参数字段失败，无法启用稳定本地缓存键。", exception);
        }
    }
}
