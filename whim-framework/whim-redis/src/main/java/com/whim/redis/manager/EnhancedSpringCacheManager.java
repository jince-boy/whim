package com.whim.redis.manager;

import com.whim.redis.utils.RedisUtils;
import lombok.Setter;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jince
 * @date 2025/7/17 23:06
 * @description 增强版 Spring CacheManager 实现，基于 Redisson 并支持多参数配置
 * 主要优化点：
 * 1. 支持更灵活的缓存配置参数
 * 2. 添加了本地缓存和分布式缓存的组合支持
 * 3. 改进了线程安全性
 * 4. 添加了更详细的文档和配置说明
 * | 示例     | 含义    | 单位           |
 * | ------ | ----- | ------------ |
 * | `30s`  | 30 秒  | seconds      |
 * | `30m`  | 30 分钟 | minutes      |
 * | `30h`  | 30 小时 | hours        |
 * | `30d`  | 30 天  | days         |
 * | `30ms` | 30 毫秒 | milliseconds |
 */
@SuppressWarnings("unchecked")
public class EnhancedSpringCacheManager implements CacheManager {

    private boolean dynamic = true;

    @Setter
    private boolean allowNullValues = true;

    @Setter
    private boolean transactionAware = true;

    private Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();

    /**
     * Creates CacheManager supplied by Redisson instance
     */
    public EnhancedSpringCacheManager() {
    }

    /**
     * Defines 'fixed' cache names.
     * A new cache instance will not be created in dynamic for non-defined names.
     * <p>
     * `null` parameter setups dynamic mode
     *
     * @param names of caches
     */
    public void setCacheNames(Collection<String> names) {
        if (names != null) {
            for (String name : names) {
                getCache(name);
            }
            dynamic = false;
        } else {
            dynamic = true;
        }
    }

    /**
     * Set cache config mapped by cache name
     *
     * @param config object
     */
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        this.configMap = (Map<String, CacheConfig>) config;
    }

    protected CacheConfig createDefaultConfig() {
        return new CacheConfig();
    }

    @Override
    public Cache getCache(String name) {
        // 重写 cacheName 支持多参数
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];

        Cache cache = instanceMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return cache;
        }

        CacheConfig config = configMap.get(name);
        if (config == null) {
            config = createDefaultConfig();
            configMap.put(name, config);
        }

        if (array.length > 1) {
            config.setTTL(DurationStyle.detectAndParse(array[1]).toMillis());
        }
        if (array.length > 2) {
            config.setMaxIdleTime(DurationStyle.detectAndParse(array[2]).toMillis());
        }
        if (array.length > 3) {
            config.setMaxSize(Integer.parseInt(array[3]));
        }
        int local = 1;
        if (array.length > 4) {
            local = Integer.parseInt(array[4]);
        }

        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(name, config, local);
        }

        return createMapCache(name, config, local);
    }

    private Cache createMap(String name, CacheConfig config, int local) {
        RMap<Object, Object> map = RedisUtils.getClient().getMap(name);

        Cache cache = new RedissonCache(map, allowNullValues);
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    private Cache createMapCache(String name, CacheConfig config, int local) {
        RMapCache<Object, Object> map = RedisUtils.getClient().getMapCache(name);

        Cache cache = new RedissonCache(map, config, allowNullValues);
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(configMap.keySet());
    }
}

