package com.whim.core.config;

import com.whim.core.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author jince
 * date: 2025/8/17 21:35
 * description: 异步配置
 */
@Slf4j
@AutoConfiguration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 自定义 @Async 的线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        if (SpringUtils.isVirtual()) {
            ThreadFactory factory = Thread.ofVirtual()
                    .name("async-virtual-", 0)
                    .factory();
            // 使用 Executors.newThreadPerTaskExecutor 简化代码（JDK 21+）
            return Executors.newThreadPerTaskExecutor(factory);
        }
        return SpringUtils.getBean("scheduledExecutorService");
    }

    /**
     * 异步任务异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("Async method [{}] threw exception: {}", method.getName(), ex.getMessage(), ex);
                if (params != null && params.length > 0) {
                    log.error("Async method [{}] params: {}", method.getName(), Arrays.toString(params));
                }
            }
        };
    }
}
