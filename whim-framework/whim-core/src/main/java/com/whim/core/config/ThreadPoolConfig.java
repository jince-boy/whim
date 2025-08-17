package com.whim.core.config;

import com.whim.core.config.properties.ThreadPoolProperties;
import com.whim.core.utils.SpringUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/8/17 20:49
 * description:
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfig {

    /**
     * 核心线程数 = CPU核心数 + 1
     */
    private final int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 通用异步任务线程池
     */
    @Bean(name = "threadPoolTaskExecutor")
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(ThreadPoolProperties poolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("whim-async-task-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(poolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(poolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 周期性或定时任务线程池
     * 自动选择虚拟线程 or 平台线程
     */
    @Bean(name = "scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder().daemon(true);
        ThreadFactory threadFactory;
        if (SpringUtils.isVirtual()) {
            threadFactory = Thread.ofVirtual().name("virtual-schedule-", 0).factory();
        } else {
            builder.namingPattern("schedule-pool-%d");
            threadFactory = builder.build();
        }
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    log.error("Scheduled task exception: ", t);
                }
            }
        };
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        return scheduledThreadPoolExecutor;
    }

    /**
     * 应用关闭时销毁线程池
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("关闭后台任务线程池");
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdown();
                if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            }
        } catch (Exception e) {
            log.error("关闭线程池异常: ", e);
        }
    }
}
