package com.whim.web.config;

import com.whim.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/03/27
 * @description web 配置
 */
@AutoConfiguration
public class WebConfig {
    /**
     * 注册全局异常处理器
     *
     * @return 全局异常处理器
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
