package com.whim.log.config;

import com.whim.core.auth.AuthenticationContext;
import com.whim.log.aspect.LogAspect;
import com.whim.log.publisher.LoginLogPublisher;
import com.whim.log.response.OperationLogResponseHandler;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 登录事件发布、操作审计切面与异步执行器自动配置。
 */
@AutoConfiguration
@EnableAsync
public class LogAutoConfiguration {
    public static final String AUDIT_EXECUTOR = "auditLogExecutor";

    /**
     * 注册登录结果事件发布器。
     *
     * @param eventPublisher Spring 事件发布器
     * @return 登录日志发布器
     */
    @Bean
    @ConditionalOnMissingBean
    public LoginLogPublisher loginLogPublisher(ApplicationEventPublisher eventPublisher) {
        return new LoginLogPublisher(eventPublisher);
    }

    /**
     * 注册控制器操作审计切面。
     *
     * @param authenticationContext 应用提供的操作者认证上下文，可空
     * @param eventPublisher Spring 事件发布器
     * @param responseHandler 应用提供的响应处理器，可空
     * @return 操作日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    public LogAspect logAspect(@Nullable AuthenticationContext authenticationContext,
                               ApplicationEventPublisher eventPublisher,
                               @Nullable OperationLogResponseHandler responseHandler) {
        return new LogAspect(authenticationContext, eventPublisher, responseHandler);
    }

    /**
     * 创建容量受限的日志执行器，队列满时由发布端记录投递失败。
     *
     * @return 日志专用线程池
     */
    @Bean(name = AUDIT_EXECUTOR, defaultCandidate = false)
    @ConditionalOnMissingBean(name = AUDIT_EXECUTOR)
    public ThreadPoolTaskExecutor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("whim-audit-log-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        return executor;
    }
}
