package com.whim.redis.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RTopic;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.UUID;
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
    private static final String WILDCARD = "*";
    private static final Object NULL_HOLDER = new Object();

    private final String name;
    private final Cache delegate;

    @Getter
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache;
    private final RTopic invalidationTopic;
    private final String nodeId;

    /**
     * 创建二级缓存装饰器
     *
     * @param name             缓存名称
     * @param delegate         底层 Redis 缓存实现
     * @param localCache       当前缓存专属的 Caffeine 本地缓存实例
     * @param invalidationTopic Redisson 缓存失效广播主题
     * @param nodeId           当前节点唯一标识，用于过滤自身广播
     */
    public CaffeineCacheDecorator(String name,
                                  Cache delegate,
                                  com.github.benmanes.caffeine.cache.Cache<Object, Object> localCache,
                                  RTopic invalidationTopic,
                                  String nodeId) {
        this.name = name;
        this.delegate = delegate;
        this.localCache = localCache;
        this.invalidationTopic = invalidationTopic;
        this.nodeId = nodeId;
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
     * 将缓存键统一规范为字符串，确保本地缓存、Redis 缓存与广播失效使用同一套键。
     *
     * @param key 原始缓存键
     * @return 规范化后的字符串缓存键
     */
    private String normalizeCacheKey(Object key) {
        if (key == null) {
            return "null";
        }
        if (key == SimpleKey.EMPTY) {
            return "SimpleKey []";
        }
        if (isSupportedStringKeyType(key) || key instanceof SimpleKey) {
            return String.valueOf(key);
        }
        throw new IllegalArgumentException(
                "缓存[%s]的键类型[%s]不支持直接转换为字符串键，请显式使用字符串键或在缓存注解中指定 key 表达式。"
                        .formatted(name, key.getClass().getName()));
    }

    /**
     * 获取缓存名称。
     */
    @Override
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * 获取底层原生缓存实现。
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
        String cacheKey = normalizeCacheKey(key);
        Object cachedValue = getLocalCachedValue(cacheKey);
        if (cachedValue != null || localCache.asMap().containsKey(cacheKey)) {
            return (T) cachedValue;
        }
        T loadedValue = delegate.get(cacheKey, valueLoader);
        localCache.put(cacheKey, toStoreValue(loadedValue));
        return loadedValue;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        String cacheKey = normalizeCacheKey(key);
        delegate.put(cacheKey, value);
        localCache.put(cacheKey, toStoreValue(value));
        publishInvalidation(cacheKey);
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        String cacheKey = normalizeCacheKey(key);
        ValueWrapper wrapper = delegate.putIfAbsent(cacheKey, value);
        if (wrapper == null) {
            localCache.put(cacheKey, toStoreValue(value));
            publishInvalidation(cacheKey);
        } else {
            localCache.put(cacheKey, toStoreValue(wrapper.get()));
        }
        return wrapper;
    }

    @Override
    public void evict(@NonNull Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(@NonNull Object key) {
        String cacheKey = normalizeCacheKey(key);
        boolean evicted = delegate.evictIfPresent(cacheKey);
        if (evicted) {
            localCache.invalidate(cacheKey);
            publishInvalidation(cacheKey);
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
    private void publishInvalidation(String cacheKey) {
        try {
            String message = nodeId + SEPARATOR + name + SEPARATOR + cacheKey;
            invalidationTopic.publish(message);
        } catch (Exception e) {
            log.warn("缓存失效广播发送失败，缓存名称：{}，缓存键：{}", name, cacheKey, e);
        }
    }

    /**
     * 获取缓存值，优先读取本地缓存，未命中时再回源 Redis。
     *
     * @param key 缓存键
     * @return 缓存值，未命中时返回 null
     */
    private Object getCachedValue(Object key) {
        String cacheKey = normalizeCacheKey(key);
        Object cachedValue = getLocalCachedValue(cacheKey);
        if (cachedValue != null || localCache.asMap().containsKey(cacheKey)) {
            return cachedValue;
        }
        ValueWrapper wrapper = delegate.get(cacheKey);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        localCache.put(cacheKey, toStoreValue(value));
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
     * 判断是否为支持直接转换为字符串的缓存键类型。
     *
     * @param key 缓存键
     * @return 是否为支持的字符串键类型
     */
    private boolean isSupportedStringKeyType(Object key) {
        return key instanceof CharSequence
                || key instanceof Number
                || key instanceof Boolean
                || key instanceof Character
                || key instanceof Enum<?>
                || key instanceof UUID
                || key instanceof Class<?>;
    }
}
