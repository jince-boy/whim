package com.whim.core.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author jince
 * date: 2025/8/17 21:11
 * description: 线程池属性
 */
@Data
@Validated
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {
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
     * 通用异步线程名前缀。
     */
    @NotBlank
    private String threadNamePrefix = "whim-async-task-";

    /**
     * 虚拟线程模式下，@Async 线程名前缀。
     */
    @NotBlank
    private String virtualThreadNamePrefix = "whim-async-virtual-";

    /**
     * 是否允许核心线程超时回收。
     */
    private boolean allowCoreThreadTimeout = false;

    /**
     * 定时调度线程池大小。
     */
    @Min(1)
    private int scheduledPoolSize = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * 平台线程模式下，调度线程名前缀。
     */
    @NotBlank
    private String scheduledThreadNamePrefix = "whim-schedule-pool-";

    /**
     * 虚拟线程模式下，调度线程名前缀。
     */
    @NotBlank
    private String virtualScheduledThreadNamePrefix = "whim-virtual-schedule-";

    /**
     * 是否在取消任务时及时从调度队列中移除。
     */
    private boolean removeOnCancelPolicy = true;

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
