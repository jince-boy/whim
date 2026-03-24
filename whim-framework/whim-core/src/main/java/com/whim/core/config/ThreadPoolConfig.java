package com.whim.core.config;

import com.whim.core.config.properties.ThreadPoolProperties;
import com.whim.core.utils.SpringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jince
 * date: 2026/3/23 23:03
 * description: 线程池配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolProperties.class)
@RequiredArgsConstructor
public class ThreadPoolConfig {
    private final ThreadPoolProperties poolProperties;

    /**
     * 显式使用的平台线程池。
     */
    @Bean(name = {"platformTaskExecutor", "threadPoolTaskExecutor"})
    @ConditionalOnMissingBean(name = {"platformTaskExecutor", "threadPoolTaskExecutor"})
    public ThreadPoolTaskExecutor platformTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 线程名前缀
        executor.setThreadNamePrefix(poolProperties.getThreadNamePrefix());
        // 核心线程数
        executor.setCorePoolSize(poolProperties.getCorePoolSize());
        // 最大线程数
        executor.setMaxPoolSize(Math.max(poolProperties.getMaxPoolSize(), poolProperties.getCorePoolSize()));
        // 队列容量
        executor.setQueueCapacity(poolProperties.getQueueCapacity());
        // 非核心线程空闲存活时间
        executor.setKeepAliveSeconds(poolProperties.getKeepAliveSeconds());
        // 是否允许核心线程超时回收
        executor.setAllowCoreThreadTimeOut(poolProperties.isAllowCoreThreadTimeout());
        // 关闭线程池时，是否等待任务执行完成
        executor.setWaitForTasksToCompleteOnShutdown(poolProperties.isWaitForTasksToCompleteOnShutdown());
        // 等待终止的最大时间（秒）
        executor.setAwaitTerminationSeconds(poolProperties.getAwaitTerminationSeconds());
        // 拒绝策略：由调用线程执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 默认任务执行器。
     * 开启虚拟线程时优先使用虚拟线程；否则普通线程池。
     */
    @Primary
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor applicationTaskExecutor(
            @Qualifier("platformTaskExecutor") ThreadPoolTaskExecutor executor) {
        if (SpringUtils.isVirtual()) {
            SimpleAsyncTaskExecutor virtualExecutor = new SimpleAsyncTaskExecutor(poolProperties.getVirtualThreadNamePrefix());
            virtualExecutor.setVirtualThreads(true);
            virtualExecutor.setTaskTerminationTimeout(poolProperties.getAwaitTerminationSeconds() * 1000L);
            return virtualExecutor;
        }
        return executor;
    }

    /**
     * @Scheduled 使用的调度器。
     */
    @Bean(name = "taskScheduler")
    @ConditionalOnMissingBean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        if (SpringUtils.isVirtual()) {
            SimpleAsyncTaskScheduler scheduler = new SimpleAsyncTaskScheduler();
            scheduler.setVirtualThreads(true);
            scheduler.setThreadNamePrefix(poolProperties.getVirtualScheduledThreadNamePrefix());
            scheduler.setTaskTerminationTimeout(poolProperties.getAwaitTerminationSeconds() * 1000L);
            scheduler.setErrorHandler(loggingErrorHandler("scheduled"));
            return scheduler;
        }
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolProperties.getScheduledPoolSize());
        scheduler.setThreadNamePrefix(poolProperties.getScheduledThreadNamePrefix());
        scheduler.setWaitForTasksToCompleteOnShutdown(poolProperties.isWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds(poolProperties.getAwaitTerminationSeconds());
        scheduler.setRemoveOnCancelPolicy(poolProperties.isRemoveOnCancelPolicy());
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.setErrorHandler(loggingErrorHandler("scheduled"));
        scheduler.initialize();
        return scheduler;
    }

    private ErrorHandler loggingErrorHandler(String executorName) {
        return throwable -> log.error("{} task execution failed", executorName, throwable);
    }
}
