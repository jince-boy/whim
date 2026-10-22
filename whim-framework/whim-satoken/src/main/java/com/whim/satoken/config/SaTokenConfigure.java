package com.whim.satoken.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.UnauthenticatedException;
import com.whim.satoken.context.AuthContext;
import com.whim.satoken.security.StpAuthManager;
import com.whim.satoken.session.SaTokenAuthenticationSession;
import com.whim.satoken.service.StpInterfaceImpl;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/04/13
 * @description Sa-Token 自动配置类，负责注册多账号体系、JWT 集成、认证上下文与认证会话组件。
 */
@AutoConfiguration
@ConfigurationProperties(prefix = "sa-token")
@EnableConfigurationProperties(SaTokenConfigure.class)
public class SaTokenConfigure implements WebMvcConfigurer {

    /**
     * 拦截器不校验登录的路径（Ant 风格），对应 YAML {@code sa-token.exclude-paths}。
     */
    @Getter
    @Setter
    private List<String> excludePaths = new ArrayList<>();

    /**
     * 注册 Sa-Token 拦截器；放行路径由 {@code sa-token.exclude-paths} 配置。
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/system/**").check(r -> StpAuthManager.SYSTEM.checkLogin());
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths);
    }

    /** 将拦截器、权限注解及业务调用的认证异常转换后交给原有 MVC 处理链，保留解析顺序。 */
    @Override
    public void extendHandlerExceptionResolvers(@NonNull List<HandlerExceptionResolver> resolvers) {
        resolvers.replaceAll(resolver -> (request, response, handler, exception) ->
                resolver.resolveException(request, response, handler, translateAuthenticationException(exception)));
    }

    /**
     * Sa-Token 整合 JWT（Simple 模式）。
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 重写 Sa-Token 注解处理器，支持 Spring 注解合并（如元注解组合）。
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }

    /**
     * 将 {@link StpAuthManager} 中所有账号体系的 StpLogic 注册到 Sa-Token。
     * 新增账号体系时，只需在 {@link StpAuthManager#allRegistered()} 中追加，此处无需改动。
     */
    @Bean
    public List<StpLogic> registeredStpLogics() {
        return StpAuthManager.allRegistered();
    }

    /**
     * 注册 Sa-Token 权限查询适配器。
     *
     * @return Sa-Token 权限查询接口实现
     */
    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }

    /**
     * 注册认证上下文 Bean，对外暴露 {@link AuthenticationContext} 接口。
     * 业务代码通过注入 {@link AuthenticationContext} 获取当前登录身份信息。
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationContext.class)
    public AuthenticationContext authenticationContext() {
        return new AuthContext();
    }

    /**
     * 注册认证会话 Bean，对外暴露登录、退出和令牌创建能力。
     *
     * @return Sa-Token 认证会话实现
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationSession.class)
    public AuthenticationSession authenticationSession() {
        return new SaTokenAuthenticationSession();
    }

    /** 仅转换明确的认证和授权失败，兼容包装异常，其余异常保持原样。 */
    private Exception translateAuthenticationException(Exception exception) {
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 20; depth++) {
            if (cause instanceof UnauthenticatedException || cause instanceof AccessDeniedException) {
                return exception;
            }
            if (cause instanceof NotLoginException notLogin) {
                String message = switch (notLogin.getType()) {
                    case NotLoginException.NOT_TOKEN -> "未能读取到有效 token";
                    case NotLoginException.INVALID_TOKEN -> "token 无效";
                    case NotLoginException.TOKEN_TIMEOUT -> "token 已过期";
                    case NotLoginException.BE_REPLACED -> "token 已被顶下线";
                    case NotLoginException.KICK_OUT -> "token 已被踢下线";
                    case NotLoginException.TOKEN_FREEZE -> "token 已被冻结";
                    case NotLoginException.NO_PREFIX -> "未按照指定前缀提交 token";
                    default -> "当前会话未登录";
                };
                return new UnauthenticatedException(message, exception);
            }
            if (cause instanceof NotPermissionException || cause instanceof NotRoleException) {
                return new AccessDeniedException("用户没有权限", exception);
            }
            if (cause instanceof NotSafeException) {
                return new AccessDeniedException("请先完成二级认证", exception);
            }
            cause = cause.getCause();
        }
        return exception;
    }
}
