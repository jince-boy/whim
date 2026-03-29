package com.whim.web.config;

import com.whim.json.config.properties.DateTimeProperties;
import com.whim.web.converter.StringToLocalDateTimeConverter;
import com.whim.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    /**
     * 注册字符串到 LocalDateTime 的转换器。
     *
     * @param dateTimeProperties 时间配置属性
     * @return LocalDateTime 转换器
     */
    @Bean
    @ConditionalOnMissingBean
    public StringToLocalDateTimeConverter stringToLocalDateTimeConverter(DateTimeProperties dateTimeProperties) {
        return new StringToLocalDateTimeConverter(dateTimeProperties);
    }

    /**
     * 注册 MVC 参数转换器。
     *
     * @param stringToLocalDateTimeConverter LocalDateTime 转换器
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer(StringToLocalDateTimeConverter stringToLocalDateTimeConverter) {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(org.springframework.format.FormatterRegistry registry) {
                registry.addConverter(stringToLocalDateTimeConverter);
            }
        };
    }
}
