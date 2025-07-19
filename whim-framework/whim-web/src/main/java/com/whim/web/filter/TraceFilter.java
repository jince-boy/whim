package com.whim.web.filter;

import com.whim.core.utils.IPUtils;
import com.whim.core.utils.TraceIdUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author jince
 * @date 2025/6/19 13:45
 * @description 链路追踪过滤器
 */
@Slf4j
public class TraceFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        log.info("IP地址为:[ {} ]的设备通过[ {} ]方法请求了路由:{ {} }", IPUtils.getClientIpAddress(request), request.getMethod(), request.getRequestURI());

        String traceId = request.getHeader(TraceIdUtils.TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = TraceIdUtils.generateTraceId();
        }
        try (TraceIdUtils.TraceIdContext traceIdContext = new TraceIdUtils.TraceIdContext(traceId)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
