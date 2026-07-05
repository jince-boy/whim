package com.whim.web.config;

import com.whim.json.config.properties.DateTimeProperties;
import com.whim.web.converter.StringToLocalDateTimeConverter;
import com.whim.web.handler.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jince
 * @date 2026/04/08
 * @description Web 模块通用配置。
 */
@AutoConfiguration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DateTimeProperties dateTimeProperties;

    /**
     * 注册全局异常处理器。
     *
     * @return 全局异常处理器
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 注册接口前缀注解映射支持。
     *
     * @return Spring MVC 组件注册扩展
     */
    @Bean
    public ApiPrefixWebMvcRegistrations apiPrefixWebMvcRegistrations() {
        return new ApiPrefixWebMvcRegistrations();
    }

    /**
     * 向 Spring MVC 注册时间字符串转换器。
     *
     * @param registry Spring MVC 格式化注册表
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter(this.dateTimeProperties));
    }
}
