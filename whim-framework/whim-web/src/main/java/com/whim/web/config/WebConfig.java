package com.whim.web.config;

import com.whim.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author jince
 * @date 2026/3/27
 * @description web 配置
 */
@AutoConfiguration
public class WebConfig {
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
