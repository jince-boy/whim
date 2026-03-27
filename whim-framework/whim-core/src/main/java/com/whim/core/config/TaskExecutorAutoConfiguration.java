package com.whim.core.config;

import com.whim.core.config.properties.TaskExecutorProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 计算任务执行器自动配置
 */
@AutoConfiguration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@ConditionalOnProperty(prefix = "whim.task", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TaskExecutorProperties.class)
public class TaskExecutorAutoConfiguration {
    /**
     * 创建计算任务执行器
     *
     * @param properties 线程池配置属性
     * @return 计算任务执行器
     */
    @Bean(name = "computeTaskExecutor", defaultCandidate = false)
    @ConditionalOnMissingBean(name = "computeTaskExecutor")
    public ThreadPoolTaskExecutor computeTaskExecutor(TaskExecutorProperties properties) {
        var poolSize = resolvePoolSize(properties);
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(Math.max(0, properties.getQueueCapacity()));
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(properties.isAwaitTermination());
        executor.setAwaitTerminationMillis(properties.getAwaitTerminationPeriod().toMillis());
        executor.initialize();
        return executor;
    }

    /**
     * 解析线程池大小
     *
     * @param properties 线程池配置属性
     * @return 线程池大小
     */
    private int resolvePoolSize(TaskExecutorProperties properties) {
        if (properties.getPoolSize() > 0) {
            return properties.getPoolSize();
        }
        return Runtime.getRuntime().availableProcessors();
    }
}
