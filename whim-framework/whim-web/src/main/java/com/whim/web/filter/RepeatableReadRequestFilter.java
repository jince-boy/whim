package com.whim.web.filter;

import com.whim.web.wrapper.RepeatableReadHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 为请求提供可重复读取的请求体
 */
public class RepeatableReadRequestFilter extends OncePerRequestFilter {

    private static final Set<String> CACHEABLE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request instanceof RepeatableReadHttpServletRequestWrapper
                || !CACHEABLE_METHODS.contains(request.getMethod())
                || isMultipartRequest(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new RepeatableReadHttpServletRequestWrapper(request), response);
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }
}
