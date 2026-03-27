package com.whim.core.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author jince
 * date: 2026/3/27 22:16
 * description:
 */
@Data
@Validated
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {
    /**
     * 是否启用线程池。
     */
    private boolean enabled = false;
    /**
     * 通用异步线程名前缀。
     */
    @NotBlank
    private String threadNamePrefix = "whim-async-task-";
    /**
     * 通用异步线程池核心线程数。
     */
    @Min(1)
    private int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 通用异步线程池最大线程数。
     */
    @Min(1)
    private int maxPoolSize = corePoolSize * 2;

    /**
     * 通用异步线程池队列容量。
     */
    @Min(0)
    private int queueCapacity = 128;

    /**
     * 非核心线程空闲存活时间，单位秒。
     */
    @Min(0)
    private int keepAliveSeconds = 300;

    /**
     * 是否允许核心线程超时回收。
     */
    private boolean allowCoreThreadTimeout = false;

    /**
     * 关闭线程池时是否等待任务执行完成。
     */
    private boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * 关闭线程池时最长等待秒数。
     */
    @Min(0)
    private int awaitTerminationSeconds = 10;
}
