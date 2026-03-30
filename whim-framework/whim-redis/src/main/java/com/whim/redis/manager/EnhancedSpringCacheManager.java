package com.whim.redis.manager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jince
 * @date 2026/03/30
 * @description 增强版 Spring CacheManager 实现，基于 Redisson 并支持 Caffeine 二级缓存。
 * <p>
 * 核心特性：
 * <ul>
 *   <li>缓存名称支持 {@code #} 分隔的多参数配置语法</li>
 *   <li>可选的 Caffeine 本地缓存（L1）+ Redis 远程缓存（L2）组合</li>
 *   <li>多节点间通过 Pub/Sub 实现本地缓存最终一致性</li>
 *   <li>线程安全的缓存实例创建</li>
 * </ul>
 * <p>
 * 缓存名称语法：{@code cacheName#ttl#maxIdleTime#maxSize#localCache}
 * <table>
 *   <tr><th>参数</th><th>说明</th><th>示例</th></tr>
 *   <tr><td>cacheName</td><td>缓存名称（必填）</td><td>users</td></tr>
 *   <tr><td>ttl</td><td>存活时间</td><td>30s, 5m, 1h, 1d</td></tr>
 *   <tr><td>maxIdleTime</td><td>最大空闲时间</td><td>10m</td></tr>
 *   <tr><td>maxSize</td><td>最大条目数</td><td>500</td></tr>
 *   <tr><td>localCache</td><td>1=启用本地缓存（默认），0=仅 Redis</td><td>0</td></tr>
 * </table>
 */
@Slf4j
public class EnhancedSpringCacheManager implements CacheManager, InitializingBean, DisposableBean {

    private final RedissonClient redissonClient;
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
    private final String nodeId = UUID.randomUUID().toString();
    private final ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();
    private final Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();

    private boolean dynamic = true;

    @Setter
    private boolean allowNullValues = true;

    @Setter
    private boolean transactionAware = true;

    private RTopic invalidationTopic;

    /**
     * 创建缓存管理器
     *
     * @param redissonClient Redisson 客户端
     * @param caffeineCache  Caffeine 本地缓存实例，为 null 则不启用二级缓存
     */
    public EnhancedSpringCacheManager(RedissonClient redissonClient,
                                      com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache) {
        this.redissonClient = redissonClient;
        this.caffeineCache = caffeineCache;
    }

    /**
     * 设置固定的缓存名称集合。设置后将关闭动态缓存创建，传入 null 则恢复动态模式。
     *
     * @param names 缓存名称集合
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
     * 设置预定义的缓存配置
     *
     * @param config 缓存名称到配置的映射
     */
    @SuppressWarnings("unchecked")
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        this.configMap.putAll((Map<String, CacheConfig>) config);
    }

    /**
     * 初始化时订阅缓存失效广播主题
     */
    @Override
    public void afterPropertiesSet() {
        if (caffeineCache == null) {
            return;
        }
        invalidationTopic = redissonClient.getTopic(
                CaffeineCacheDecorator.getInvalidationTopicName(), StringCodec.INSTANCE);
        invalidationTopic.addListener(String.class, (channel, message) -> handleInvalidationMessage(message));
        log.info("缓存失效广播订阅成功，节点标识：{}", nodeId);
    }

    /**
     * 销毁时移除所有监听器
     */
    @Override
    public void destroy() {
        if (invalidationTopic != null) {
            invalidationTopic.removeAllListeners();
        }
    }

    @Override
    public Cache getCache(String name) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        String cacheName = array[0];

        Cache cache = instanceMap.get(cacheName);
        if (cache != null) {
            return cache;
        }
        if (!dynamic) {
            return null;
        }

        return instanceMap.computeIfAbsent(cacheName, k -> {
            CacheConfig config = configMap.computeIfAbsent(k, ignored -> new CacheConfig());
            boolean enableLocalCache = true;
            if (array.length > 1) {
                config.setTTL(DurationStyle.detectAndParse(array[1]).toMillis());
            }
            if (array.length > 2) {
                config.setMaxIdleTime(DurationStyle.detectAndParse(array[2]).toMillis());
            }
            if (array.length > 3) {
                config.setMaxSize(Integer.parseInt(array[3]));
            }
            if (array.length > 4) {
                enableLocalCache = Integer.parseInt(array[4]) == 1;
            }
            boolean useTimed = config.getMaxIdleTime() > 0 || config.getTTL() > 0 || config.getMaxSize() > 0;
            return useTimed
                    ? createMapCache(k, config, enableLocalCache)
                    : createMap(k, config, enableLocalCache);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(instanceMap.keySet());
    }

    /**
     * 创建无过期策略的缓存实例
     *
     * @param name             缓存名称
     * @param config           缓存配置
     * @param enableLocalCache 是否启用本地缓存
     * @return 缓存实例
     */
    private Cache createMap(String name, CacheConfig config, boolean enableLocalCache) {
        RMap<Object, Object> map = redissonClient.getMap(name);
        Cache cache = new RedissonCache(map, allowNullValues);
        return wrapCache(name, cache, enableLocalCache);
    }

    /**
     * 创建带过期策略的缓存实例
     *
     * @param name             缓存名称
     * @param config           缓存配置
     * @param enableLocalCache 是否启用本地缓存
     * @return 缓存实例
     */
    private Cache createMapCache(String name, CacheConfig config, boolean enableLocalCache) {
        RMapCache<Object, Object> map = redissonClient.getMapCache(name);
        Cache cache = new RedissonCache(map, config, allowNullValues);
        if (config.getMaxSize() > 0) {
            map.setMaxSize(config.getMaxSize());
        }
        return wrapCache(name, cache, enableLocalCache);
    }

    /**
     * 按需包装缓存（本地缓存装饰 + 事务感知装饰）
     *
     * @param name             缓存名称
     * @param cache            原始缓存实例
     * @param enableLocalCache 是否启用本地缓存
     * @return 包装后的缓存实例
     */
    private Cache wrapCache(String name, Cache cache, boolean enableLocalCache) {
        if (enableLocalCache && caffeineCache != null && invalidationTopic != null) {
            cache = new CaffeineCacheDecorator(name, cache, caffeineCache, invalidationTopic, nodeId);
        }
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        return cache;
    }

    /**
     * 处理缓存失效广播消息。消息格式：nodeId::cacheName::key
     *
     * @param message 广播消息
     */
    private void handleInvalidationMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        String[] parts = message.split("::", 3);
        if (parts.length < 3) {
            return;
        }
        String senderNodeId = parts[0];
        if (nodeId.equals(senderNodeId)) {
            return;
        }
        String cacheName = parts[1];
        String key = parts[2];
        if ("*".equals(key)) {
            caffeineCache.asMap().keySet().removeIf(k -> k.toString().startsWith(cacheName + ":"));
        } else {
            caffeineCache.invalidate(cacheName + ":" + key);
        }
    }
}
