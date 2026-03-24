package com.whim.core.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * @author jince
 * date: 2025/8/17 21:35
 * description: 异步配置
 */
@Slf4j
@AutoConfiguration
@AutoConfigureAfter(ThreadPoolConfig.class)
@ConditionalOnMissingBean(AsyncConfigurer.class)
public class AsyncConfig implements AsyncConfigurer {
    private final Executor asyncExecutor;

    public AsyncConfig(@Qualifier("applicationTaskExecutor") Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * 自定义 @Async 的线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    /**
     * 异步任务异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(@NonNull Throwable ex, @NonNull Method method, Object @NonNull ... params) {
                log.error("Async method [{}] threw exception", method.getName(), ex);
                if (params.length > 0) {
                    log.error("Async method [{}] params: {}", method.getName(), Arrays.toString(params));
                }
            }
        };
    }
}
