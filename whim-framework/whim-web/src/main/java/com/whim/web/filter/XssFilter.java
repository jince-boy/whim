package com.whim.web.filter;

import com.whim.core.utils.SpringUtils;
import com.whim.web.config.properties.XssProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jince
 * @date 2025/6/24 15:10
 * @description 跨站脚本过滤器
 */
public class XssFilter implements Filter {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> excludeUrls = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        XssProperties properties = SpringUtils.getBean(XssProperties.class);
        excludeUrls.addAll(properties.getExcludeUrls());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        if (shouldSkip(httpRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        filterChain.doFilter(new XssRequestWrapper(httpRequest), servletResponse);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludeUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
