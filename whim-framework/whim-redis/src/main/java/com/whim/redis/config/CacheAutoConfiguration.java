package com.whim.redis.config;

import com.whim.redis.config.properties.CaffeineProperties;
import com.whim.redis.manager.EnhancedSpringCacheManager;
import com.whim.redis.utils.CacheUtils;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

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
     * 创建增强版缓存管理器，根据配置决定是否启用本地二级缓存。
     *
     * @param redissonClient Redisson 客户端
     * @param jsonMapper Spring 管理的 JsonMapper
     * @return 缓存管理器
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedissonClient redissonClient, JsonMapper jsonMapper) {
        return new EnhancedSpringCacheManager(redissonClient, caffeineProperties, jsonMapper);
    }

    /**
     * 初始化 CacheUtils 使用的 CacheManager。
     *
     * @param cacheManager 缓存管理器
     * @return CacheUtils 初始化器
     */
    @Bean
    public CacheUtils.Initializer cacheUtilsInitializer(CacheManager cacheManager) {
        return new CacheUtils.Initializer(cacheManager);
    }
}
