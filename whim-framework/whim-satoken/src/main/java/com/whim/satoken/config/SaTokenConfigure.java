package com.whim.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.whim.core.auth.AuthenticationContext;
import com.whim.satoken.context.AuthContext;
import com.whim.satoken.handler.SaTokenExceptionHandler;
import com.whim.satoken.security.StpAuthManager;
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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/04/13
 * @description Sa-Token 自动配置类，负责注册多账号体系、JWT 集成及认证上下文组件。
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
     * 注册认证上下文 Bean，对外暴露 {@link AuthenticationContext} 接口。
     * 业务代码通过注入 {@link AuthenticationContext} 获取当前登录用户信息。
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationContext.class)
    public AuthenticationContext authenticationContext() {
        return new AuthContext();
    }

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}
