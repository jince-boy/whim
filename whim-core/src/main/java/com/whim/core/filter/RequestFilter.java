package com.whim.core.filter;

import com.whim.common.utils.IPUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Jince
 * date: 2024/10/20 02:22
 * description: 全局请求过滤器
 */
@Slf4j
public class RequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info("IP地址为:[ {} ]的设备通过[ {} ]方法请求了路由:{ {} }", IPUtil.getClientIpAddress(request), request.getMethod(), request.getRequestURI());
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
