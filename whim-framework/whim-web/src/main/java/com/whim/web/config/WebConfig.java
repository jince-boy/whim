package com.whim.web.config;

import com.whim.json.config.properties.DateTimeProperties;
import com.whim.web.converter.StringToLocalDateTimeConverter;
import com.whim.web.converter.XssStringConverter;
import com.whim.web.handler.GlobalExceptionHandler;
import com.whim.web.resolver.XssMethodArgumentResolver;
import com.whim.web.xss.XssSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC configuration.
 */
@AutoConfiguration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DateTimeProperties dateTimeProperties;
    private final XssSanitizer xssSanitizer;

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public StringToLocalDateTimeConverter stringToLocalDateTimeConverter(DateTimeProperties dateTimeProperties) {
        return new StringToLocalDateTimeConverter(dateTimeProperties);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateTimeConverter(this.dateTimeProperties));
        registry.addConverter(new XssStringConverter(this.xssSanitizer));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new XssMethodArgumentResolver(this.xssSanitizer));
    }
}
