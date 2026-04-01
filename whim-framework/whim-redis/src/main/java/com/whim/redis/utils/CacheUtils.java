package com.whim.redis.utils;

import com.whim.core.utils.SpringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

/**
 * 缓存工具类，统一封装 Spring Cache 的常用操作。
 */
public final class CacheUtils {

    private CacheUtils() {
    }

    private static CacheManager cacheManager() {
        return SpringUtils.getBean(CacheManager.class);
    }

    private static Cache requireCache(String cacheName) {
        return Objects.requireNonNull(cacheManager().getCache(cacheName), "缓存[%s]不存在".formatted(cacheName));
    }

    public static <T> T get(String cacheName, Object key) {
        Cache.ValueWrapper wrapper = requireCache(cacheName).get(key);
        if (wrapper == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T value = (T) wrapper.get();
        return value;
    }

    public static <T> T get(String cacheName, Object key, Class<T> type) {
        return requireCache(cacheName).get(key, type);
    }

    public static void put(String cacheName, Object key, Object value) {
        requireCache(cacheName).put(key, value);
    }

    public static void evict(String cacheName, Object key) {
        requireCache(cacheName).evict(key);
    }

    public static void clear(String cacheName) {
        requireCache(cacheName).clear();
    }
}
