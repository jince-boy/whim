package com.whim.core.config.properties;

import java.time.Duration;
import java.util.Objects;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 任务执行器配置属性
 */
@Getter
@ConfigurationProperties(prefix = "whim.task")
public final class TaskExecutorProperties {
    private final boolean enabled;
    private final int poolSize;
    private final int queueCapacity;
    private final String threadNamePrefix;
    private final boolean awaitTermination;
    private final Duration awaitTerminationPeriod;

    /**
     * 创建任务执行器配置属性
     *
     * @param enabled 是否启用任务执行器
     * @param poolSize 线程池大小
     * @param queueCapacity 队列容量
     * @param threadNamePrefix 线程名前缀
     * @param awaitTermination 关闭时是否等待任务完成
     * @param awaitTerminationPeriod 关闭等待时长
     */
    public TaskExecutorProperties(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("8") int poolSize,
            @DefaultValue("200") int queueCapacity,
            @DefaultValue("whim-compute-") String threadNamePrefix,
            @DefaultValue("true") boolean awaitTermination,
            @DefaultValue("10s") Duration awaitTerminationPeriod) {
        this.enabled = enabled;
        this.poolSize = poolSize;
        this.queueCapacity = queueCapacity;
        this.threadNamePrefix = Objects.requireNonNull(threadNamePrefix, "threadNamePrefix must not be null");
        this.awaitTermination = awaitTermination;
        this.awaitTerminationPeriod = Objects.requireNonNull(
                awaitTerminationPeriod,
                "awaitTerminationPeriod must not be null");
    }
}
