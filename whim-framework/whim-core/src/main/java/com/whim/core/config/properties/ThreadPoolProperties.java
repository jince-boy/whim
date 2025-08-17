package com.whim.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jince
 * date: 2025/8/17 21:11
 * description: 线程池属性
 */
@Data
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {
    /**
     * 是否启用线程池
     */
    private boolean enabled = false;
    /**
     * 队列最大长度
     */
    private int queueCapacity = 128;
    /**
     * 线程池维护线程所允许的空闲时间
     */
    private int keepAliveSeconds = 300;
}
