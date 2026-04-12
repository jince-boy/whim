package com.whim.web.config;

import com.whim.web.filter.XssFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Jince
 * @date 2026/04/08
 * @description XSS 过滤器自动配置
 */
@AutoConfiguration
public class FilterConfiguration {

    /**
     * 注册全局 XSS 过滤器。
     *
     * @param handlerMapping 主 Web MVC 的 {@link RequestMappingHandlerMapping}（与 Actuator 控制器端点映射区分）
     * @return 过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XssFilter(handlerMapping));
        registrationBean.setName("xssFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
