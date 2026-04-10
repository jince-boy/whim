package com.whim.satoken.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.whim.satoken.security.StpAuthManager;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author jince
 * date: 2026/4/8 22:44
 * description: sa-token 配置类
 */
@AutoConfiguration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
//        registry.addInterceptor(new SaInterceptor(handle -> {
//                    SaRouter.match("/system/**").check(r -> StpAuthManager.SYSTEM.checkLogin());
//                }))
//                .addPathPatterns("/**")
//                .excludePathPatterns(ExcludePathEnum.getAllEnumDetails());
    }

    /**
     * Sa-Token 整合 jwt
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 重写Sa-Token的注解处理器，增加注解合并功能
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }

    /**
     * 将 {@link StpAuthManager} 中的全部多账号 StpLogic 注册到 Sa-Token，与门面中定义的账号体系统一。
     */
    @Bean
    public List<StpLogic> registeredStpLogics() {
        return StpAuthManager.allRegistered();
    }
}
