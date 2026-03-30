package com.whim.redis.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.whim.redis.config.properties.CaffeineProperties;
import com.whim.redis.manager.EnhancedSpringCacheManager;
import com.whim.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/03/30
 * @description 缓存自动配置，提供 Caffeine 本地缓存 + Redisson 远程缓存的二级缓存方案。
 */
@AutoConfiguration(after = org.redisson.spring.starter.RedissonAutoConfiguration.class)
@ConditionalOnBean(RedissonClient.class)
@EnableCaching
@EnableConfigurationProperties(CaffeineProperties.class)
@RequiredArgsConstructor
public class CacheAutoConfiguration {

    private final CaffeineProperties caffeineProperties;

    /**
     * 创建 Caffeine 本地缓存实例。当 {@code whim.cache.caffeine.enabled=false} 时不创建。
     *
     * @return Caffeine 缓存实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "whim.cache.caffeine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Cache<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(caffeineProperties.getExpireAfterWrite())
                .initialCapacity(caffeineProperties.getInitialCapacity())
                .maximumSize(caffeineProperties.getMaximumSize())
                .build();
    }

    /**
     * 创建 Redis 操作服务
     *
     * @param redissonClient Redisson 客户端
     * @return Redis 操作服务
     */
    @Bean
    public RedisService redisService(RedissonClient redissonClient) {
        return new RedisService(redissonClient);
    }

    /**
     * 创建增强版缓存管理器。当 Caffeine 缓存 Bean 不存在时自动降级为纯 Redis 缓存。
     *
     * @param redissonClient Redisson 客户端
     * @param caffeineCache  Caffeine 本地缓存，可选注入
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient,
                                     @org.springframework.beans.factory.annotation.Autowired(required = false)
                                     Cache<Object, Object> caffeineCache) {
        return new EnhancedSpringCacheManager(redissonClient, caffeineCache);
    }
}
