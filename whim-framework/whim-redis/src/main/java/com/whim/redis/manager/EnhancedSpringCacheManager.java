package com.whim.redis.manager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.whim.redis.config.properties.CaffeineProperties;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
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
 */
@Slf4j
public class EnhancedSpringCacheManager implements CacheManager, InitializingBean, DisposableBean {

    private final RedissonClient redissonClient;
    private final CaffeineProperties caffeineProperties;
    private final JsonMapper jsonMapper;
    private final String nodeId = UUID.randomUUID().toString();
    private final ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> cacheDefinitionMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CaffeineCacheDecorator> localCacheDecoratorMap = new ConcurrentHashMap<>();
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
     * @param caffeineProperties Caffeine 本地缓存配置
     * @param jsonMapper Spring 管理的 JsonMapper
     */
    public EnhancedSpringCacheManager(RedissonClient redissonClient,
                                      CaffeineProperties caffeineProperties,
                                      JsonMapper jsonMapper) {
        this.redissonClient = redissonClient;
        this.caffeineProperties = caffeineProperties;
        this.jsonMapper = jsonMapper;
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
        if (!caffeineProperties.isEnabled()) {
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
    public Cache getCache(@NonNull String name) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        String cacheName = array[0];
        String currentDefinition = buildCacheDefinition(array);

        Cache cache = instanceMap.get(cacheName);
        if (cache != null) {
            validateCacheDefinition(cacheName, currentDefinition);
            return cache;
        }
        if (!dynamic) {
            return null;
        }

        return instanceMap.computeIfAbsent(cacheName, k -> {
            registerCacheDefinition(cacheName, currentDefinition);
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
    @NonNull
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
        return wrapCache(name, cache, config, enableLocalCache);
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
        return wrapCache(name, cache, config, enableLocalCache);
    }

    /**
     * 按需包装缓存（本地缓存装饰 + 事务感知装饰）
     *
     * @param name             缓存名称
     * @param cache            原始缓存实例
     * @param config           缓存配置
     * @param enableLocalCache 是否启用本地缓存
     * @return 包装后的缓存实例
     */
    private Cache wrapCache(String name, Cache cache, CacheConfig config, boolean enableLocalCache) {
        if (enableLocalCache && caffeineProperties.isEnabled() && invalidationTopic != null) {
            CaffeineCacheDecorator decorator = createLocalCacheDecorator(name, cache, config);
            localCacheDecoratorMap.put(name, decorator);
            cache = decorator;
        } else {
            localCacheDecoratorMap.remove(name);
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
        CaffeineCacheDecorator cacheDecorator = localCacheDecoratorMap.get(cacheName);
        if (cacheDecorator == null) {
            return;
        }
        if ("*".equals(key)) {
            cacheDecorator.invalidateLocalAll();
        } else {
            cacheDecorator.invalidateLocalEntry(key);
        }
    }

    /**
     * 创建与 Redis 过期策略对齐的本地缓存装饰器。
     *
     * @param name   缓存名称
     * @param cache  原始缓存实例
     * @param config Redis 缓存配置
     * @return 本地缓存装饰器
     */
    private CaffeineCacheDecorator createLocalCacheDecorator(String name, Cache cache, CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .initialCapacity(caffeineProperties.getInitialCapacity())
                .maximumSize(caffeineProperties.getMaximumSize());

        Duration expireAfterWrite = resolveLocalExpireAfterWrite(config);
        if (expireAfterWrite != null) {
            builder.expireAfterWrite(expireAfterWrite);
        }

        Duration expireAfterAccess = resolveLocalExpireAfterAccess(config);
        if (expireAfterAccess != null) {
            builder.expireAfterAccess(expireAfterAccess);
        }

        return new CaffeineCacheDecorator(name, cache, builder.build(), invalidationTopic, nodeId, jsonMapper);
    }

    /**
     * 计算本地缓存的写入后过期时间，确保不会超过 Redis TTL。
     *
     * @param config Redis 缓存配置
     * @return 本地写入后过期时间
     */
    private Duration resolveLocalExpireAfterWrite(CacheConfig config) {
        Duration localDuration = normalizeDuration(caffeineProperties.getExpireAfterWrite());
        Duration redisTtl = toDuration(config.getTTL());
        if (redisTtl == null) {
            return localDuration;
        }
        if (localDuration == null) {
            return redisTtl;
        }
        return redisTtl.compareTo(localDuration) < 0 ? redisTtl : localDuration;
    }

    /**
     * 计算本地缓存的访问后过期时间，确保与 Redis MaxIdle 保持一致。
     *
     * @param config Redis 缓存配置
     * @return 本地访问后过期时间
     */
    private Duration resolveLocalExpireAfterAccess(CacheConfig config) {
        return toDuration(config.getMaxIdleTime());
    }

    /**
     * 将毫秒值转换为 Duration。
     *
     * @param millis 毫秒值
     * @return Duration 对象
     */
    private Duration toDuration(long millis) {
        if (millis <= 0) {
            return null;
        }
        return Duration.ofMillis(millis);
    }

    /**
     * 归一化 Duration，零值或负值统一视为未配置。
     *
     * @param duration 原始时长
     * @return 归一化后的时长
     */
    private Duration normalizeDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return null;
        }
        return duration;
    }

    /**
     * 构建缓存定义签名，用于校验同名缓存是否声明了不一致的配置。
     *
     * @param array 缓存名称拆分结果
     * @return 缓存定义签名
     */
    private String buildCacheDefinition(String[] array) {
        return String.join("#", array);
    }

    /**
     * 注册缓存定义，避免同名缓存在不同位置声明出不一致的 TTL 或本地缓存策略。
     *
     * @param cacheName          逻辑缓存名称
     * @param currentDefinition  当前缓存定义
     */
    private void registerCacheDefinition(String cacheName, String currentDefinition) {
        String existingDefinition = cacheDefinitionMap.putIfAbsent(cacheName, currentDefinition);
        if (existingDefinition != null && !existingDefinition.equals(currentDefinition)) {
            throw new IllegalStateException("缓存[%s]存在冲突的配置声明，请保持 TTL、MaxIdle、MaxSize 和本地缓存开关一致。"
                    .formatted(cacheName));
        }
    }

    /**
     * 校验已创建缓存的定义是否与当前请求一致。
     *
     * @param cacheName         逻辑缓存名称
     * @param currentDefinition 当前缓存定义
     */
    private void validateCacheDefinition(String cacheName, String currentDefinition) {
        String existingDefinition = cacheDefinitionMap.get(cacheName);
        if (existingDefinition != null && !existingDefinition.equals(currentDefinition)) {
            throw new IllegalStateException("缓存[%s]已存在不同配置，禁止混用不同的 TTL、MaxIdle、MaxSize 或本地缓存开关。"
                    .formatted(cacheName));
        }
    }
}
