package com.whim.core.filter;

import com.whim.common.utils.TraceIdUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Jince
 * date: 2024/10/20 02:22
 * description: 用于在请求期间追踪Trace ID的过滤器。确保每个请求都有唯一的Trace ID以便更好的日志记录和追踪。
 */
public class TraceFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String traceId = request.getHeader(TraceIdUtil.TRACE_ID);

        if (StringUtils.isBlank(traceId)) {
            traceId = TraceIdUtil.generateTraceId();
        }
        try (TraceIdUtil.TraceIdContext traceIdContext = new TraceIdUtil.TraceIdContext(traceId)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
