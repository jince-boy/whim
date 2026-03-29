package com.whim.web.config;

import com.whim.web.filter.RepeatableReadRequestFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 可重复读取请求体过滤器自动配置
 */
@AutoConfiguration
public class FilterConfiguration {

    /**
     * 注册可重复读取请求体过滤器。
     *
     * @return 过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<RepeatableReadRequestFilter> repeatableReadRequestFilter() {
        FilterRegistrationBean<RepeatableReadRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RepeatableReadRequestFilter());
        registrationBean.setName("repeatableReadRequestFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
