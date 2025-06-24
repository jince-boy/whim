package com.whim.redis.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/6/23 18:20
 * description: caffeine 本地缓存配置
 */
@AutoConfiguration
public class CacheConfig {

    // Redis缓存配置
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public CacheManager redissonCacheManager(RedissonClient redissonClient) {
        return new RedissonSpringCacheManager(redissonClient);
    }

    // 可选的Caffeine Bean（供其他组件直接使用）
    @Bean
    public Cache<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .initialCapacity(100)
                .maximumSize(1000)
                .build();
    }
}
