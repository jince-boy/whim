package com.whim.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.whim.core.factory.YmlPropertySourceFactory;
import com.whim.satoken.enums.ExcludePathConstants;
import com.whim.satoken.handler.SaTokenExceptionHandler;
import com.whim.satoken.kit.StpKit;
import com.whim.satoken.service.StpInterfaceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author jince
 * date: 2025/6/19 14:34
 * description: Sa-Token 配置
 */
@AutoConfiguration
@PropertySource(value = "classpath:sa-token.yml", factory = YmlPropertySourceFactory.class)
public class SaTokenConfig implements WebMvcConfigurer {
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
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }

    /**
     * 权限接口实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}
