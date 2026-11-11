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

/**
 * @author Jince
 * @date 2026/03/30
 * @description 缓存自动配置，提供 Caffeine 本地缓存 + Redisson 远程缓存的二级缓存方案。
 */
// Boot 4 实际导入 V4 配置，必须先注册 RedissonClient，再评估下面的 Bean 条件。
@AutoConfiguration(after = org.redisson.spring.starter.RedissonAutoConfigurationV4.class)
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
     * @return 缓存管理器
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedissonClient redissonClient) {
        return new EnhancedSpringCacheManager(redissonClient, caffeineProperties);
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
