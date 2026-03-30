package com.whim.web.config;

import com.whim.web.handler.LockExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Lock4j 异常处理自动配置。
 * 仅当 Lock4j 依赖存在于 classpath 时激活，确保 whim-web 不强依赖 Lock4j。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.lock.exception.LockFailureException")
public class LockExceptionAutoConfiguration {

    /**
     * 注册 Lock4j 分布式锁异常处理器
     *
     * @return Lock4j 异常处理器
     */
    @Bean
    public LockExceptionHandler lockExceptionHandler() {
        return new LockExceptionHandler();
    }
}
