package com.whim.web.config;

import com.whim.web.filter.XssFilter;
import com.whim.web.xss.XssCleaner;
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
     * 注册 XSS 清洗器。
     *
     * @return XSS 清洗器
     */
    @Bean
    public XssCleaner xssCleaner() {
        return new XssCleaner();
    }

    /**
     * 注册全局 XSS 过滤器。
     *
     * @param handlerMapping Spring MVC 请求映射处理器
     * @param xssCleaner XSS 清洗器
     * @return 过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter(
            RequestMappingHandlerMapping handlerMapping,
            XssCleaner xssCleaner
    ) {
        FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XssFilter(handlerMapping, xssCleaner));
        registrationBean.setName("xssFilter");
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
