package com.whim.redis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Caffeine 本地缓存配置属性
 */
@Data
@ConfigurationProperties(prefix = "whim.cache.caffeine")
public class CaffeineProperties {

    /**
     * 是否启用本地缓存（二级缓存 L1）。关闭后仅使用 Redis 单级缓存。
     */
    private boolean enabled = true;

    /**
     * 写入后过期时间()
     */
    private Duration expireAfterWrite = Duration.ofSeconds(30);

    /**
     * 初始容量
     */
    private int initialCapacity = 100;

    /**
     * 最大缓存条目数
     */
    private int maximumSize = 1000;
}
