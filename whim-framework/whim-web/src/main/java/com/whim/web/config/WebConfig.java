package com.whim.web.config;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.constant.WebPrefixConstants;
import com.whim.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author jince
 * @date 2025/6/19 16:58
 * @description web 配置
 */
@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 不同资源路径前缀配置
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(WebPrefixConstants.SYSTEM, c -> c.isAnnotationPresent(SystemApiPrefix.class));
    }

    /**
     * 全局异常处理器
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
