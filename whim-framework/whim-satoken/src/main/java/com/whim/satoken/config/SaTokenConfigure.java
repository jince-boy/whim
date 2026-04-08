package com.whim.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
}
