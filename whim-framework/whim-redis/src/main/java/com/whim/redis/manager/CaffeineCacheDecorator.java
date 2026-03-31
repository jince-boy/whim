package com.whim.redis.manager;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.cache.Cache;

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

    private final String name;
    private final Cache delegate;
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
    private final RTopic invalidationTopic;
    private final String nodeId;

    /**
     * 创建二级缓存装饰器
     *
     * @param name             缓存名称
     * @param delegate         底层 Redis 缓存实现
     * @param caffeineCache    共享的 Caffeine 本地缓存实例
     * @param invalidationTopic Redisson 缓存失效广播主题
     * @param nodeId           当前节点唯一标识，用于过滤自身广播
     */
    public CaffeineCacheDecorator(String name,
                                  Cache delegate,
                                  com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache,
                                  RTopic invalidationTopic,
                                  String nodeId) {
        this.name = name;
        this.delegate = delegate;
        this.caffeineCache = caffeineCache;
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
     * 构建 Caffeine 中使用的唯一 key
     *
     * @param key 原始缓存 key
     * @return 带缓存名称前缀的唯一 key
     */
    private String getUniqueKey(Object key) {
        return name + ":" + key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        Object cached = caffeineCache.get(getUniqueKey(key), k -> delegate.get(key));
        return (ValueWrapper) cached;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        Object cached = caffeineCache.get(getUniqueKey(key), k -> delegate.get(key, type));
        return (T) cached;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object cached = caffeineCache.get(getUniqueKey(key), k -> delegate.get(key, valueLoader));
        return (T) cached;
    }

    @Override
    public void put(Object key, Object value) {
        caffeineCache.invalidate(getUniqueKey(key));
        delegate.put(key, value);
        publishInvalidation(String.valueOf(key));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        caffeineCache.invalidate(getUniqueKey(key));
        ValueWrapper wrapper = delegate.putIfAbsent(key, value);
        publishInvalidation(String.valueOf(key));
        return wrapper;
    }

    @Override
    public void evict(Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean evicted = delegate.evictIfPresent(key);
        if (evicted) {
            caffeineCache.invalidate(getUniqueKey(key));
            publishInvalidation(String.valueOf(key));
        }
        return evicted;
    }

    @Override
    public void clear() {
        caffeineCache.asMap().keySet().removeIf(k -> k.toString().startsWith(name + ":"));
        delegate.clear();
        publishInvalidation(WILDCARD);
    }

    @Override
    public boolean invalidate() {
        boolean result = delegate.invalidate();
        caffeineCache.asMap().keySet().removeIf(k -> k.toString().startsWith(name + ":"));
        publishInvalidation(WILDCARD);
        return result;
    }

    /**
     * 广播缓存失效消息。格式：nodeId::cacheName::key
     *
     * @param key 缓存 key，使用 "*" 表示清空整个缓存
     */
    private void publishInvalidation(String key) {
        try {
            String message = nodeId + SEPARATOR + name + SEPARATOR + key;
            invalidationTopic.publish(message);
        } catch (Exception e) {
            log.warn("缓存失效广播发送失败，缓存名称：{}，key：{}", name, key, e);
        }
    }
}
