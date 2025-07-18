package com.whim.redis.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.whim.redis.manager.EnhancedSpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/6/23 18:20
 * description: caffeine 本地缓存配置
 */
@AutoConfiguration
@EnableCaching
public class CacheConfig {
    // 可选的Caffeine Bean（供其他组件直接使用）
    @Bean
    public Cache<Object, Object> caffeine() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .initialCapacity(100)
                .maximumSize(1000)
                .build();
    }

    @Bean
    public CacheManager cacheManager() {
        return new EnhancedSpringCacheManager();
    }


}
