package com.whim.core.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.whim.core.auth.enums.ExcludePathConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jince
 * date: 2024/10/24 23:10
 * description: SaToken配置类，权限拦截器
 */
@Configuration
public class SaTokenConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle-> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(ExcludePathConstants.getAllEnumDetails());
    }
}
