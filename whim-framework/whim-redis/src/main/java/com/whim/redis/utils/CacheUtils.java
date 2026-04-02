package com.whim.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/02
 * @description 缓存工具类，统一封装 Spring Cache 的常用操作。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheUtils {

    /**
     * 当前使用的 CacheManager 实例。
     */
    private static volatile CacheManager cacheManager;

    /**
     * 获取当前使用的 CacheManager 实例。
     *
     * @return CacheManager 实例
     */
    public static CacheManager getCacheManager() {
        return requireCacheManager();
    }

    /**
     * 替换当前 CacheManager 实例。
     *
     * @param cacheManager CacheManager 实例
     */
    public static void setCacheManager(CacheManager cacheManager) {
        CacheUtils.cacheManager = Objects.requireNonNull(cacheManager, "参数[cacheManager]不能为空");
    }

    /**
     * 重置当前 CacheManager 实例。
     */
    public static void resetCacheManager() {
        cacheManager = null;
    }

    /**
     * 获取指定缓存中的值。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <T>       返回值泛型
     * @return 缓存值
     */
    public static <T> T get(String cacheName, Object key) {
        Cache.ValueWrapper wrapper = requireCache(cacheName).get(key);
        if (wrapper == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T value = (T) wrapper.get();
        return value;
    }

    /**
     * 按指定类型获取缓存值。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param type      目标类型
     * @param <T>       返回值泛型
     * @return 缓存值
     */
    public static <T> T get(String cacheName, Object key, Class<T> type) {
        return requireCache(cacheName).get(key, type);
    }

    /**
     * 写入缓存值。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     */
    public static void put(String cacheName, Object key, Object value) {
        requireCache(cacheName).put(key, value);
    }

    /**
     * 删除指定缓存键。
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public static void evict(String cacheName, Object key) {
        requireCache(cacheName).evict(key);
    }

    /**
     * 清空指定缓存。
     *
     * @param cacheName 缓存名称
     */
    public static void clear(String cacheName) {
        requireCache(cacheName).clear();
    }

    /**
     * 获取指定缓存实例。
     *
     * @param cacheName 缓存名称
     * @return 缓存实例
     */
    private static Cache requireCache(String cacheName) {
        return Objects.requireNonNull(requireCacheManager().getCache(cacheName), "缓存[%s]不存在".formatted(cacheName));
    }

    /**
     * 获取当前 CacheManager 实例。
     *
     * @return CacheManager 实例
     */
    private static CacheManager requireCacheManager() {
        if (cacheManager == null) {
            throw new IllegalStateException("CacheUtils 尚未初始化 CacheManager");
        }
        return cacheManager;
    }

    /**
     * @author Jince
     * @date 2026/04/02
     * @description CacheUtils 初始化器，用于接入 Spring 管理的 CacheManager。
     */
    public static final class Initializer {

        /**
         * 初始化 CacheUtils 使用的 CacheManager。
         *
         * @param cacheManager Spring 管理的 CacheManager
         */
        public Initializer(CacheManager cacheManager) {
            CacheUtils.setCacheManager(cacheManager);
        }
    }
}
