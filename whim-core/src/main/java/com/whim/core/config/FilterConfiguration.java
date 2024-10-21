package com.whim.core.config;

import com.whim.core.filter.RequestFilter;
import com.whim.core.filter.TraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @author Jince
 * date: 2024/10/20 02:21
 * description: 过滤器配置类
 */
@Configuration
public class FilterConfiguration {
    /**
     * 全局请求过滤器
     */
    @Bean
    public FilterRegistrationBean<RequestFilter> createRequestFilterRegistrationBean() {
        FilterRegistrationBean<RequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestFilter());
        registrationBean.setName("RequestFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

    /**
     * MDC日志追踪过滤器
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> createTraceFilterRegistrationBean() {
        FilterRegistrationBean<TraceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceFilter());
        registrationBean.setName("TraceFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
