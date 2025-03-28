package com.whim.core.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.whim.core.auth.enums.ExcludePathConstants;
import com.whim.core.auth.kit.StpKit;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jince
 * date: 2024/10/24 23:10
 * description: SaToken配置类，权限拦截器
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/system/**").check(r -> StpKit.SYSTEM.checkLogin());
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(ExcludePathConstants.getAllEnumDetails());
    }

    /**
     * 重写Sa-Token默认的注解处理器，用于重写Sa-Token的注解处理器，增加注解合并功能
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功能
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}
