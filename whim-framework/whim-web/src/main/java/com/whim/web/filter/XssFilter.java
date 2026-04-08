package com.whim.web.filter;

import com.whim.web.annotation.XssIgnore;
import com.whim.web.wrapper.XssHttpServletRequestWrapper;
import com.whim.web.xss.XssCleaner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Jince
 * @date 2026/04/08
 * @description 提供全局 XSS 过滤能力，并支持通过注解排除指定接口。
 */
@RequiredArgsConstructor
public class XssFilter extends OncePerRequestFilter {
    private final RequestMappingHandlerMapping handlerMapping;
    private final XssCleaner xssCleaner;

    /**
     * 判断当前请求是否需要跳过 XSS 过滤。
     *
     * @param request 当前请求
     * @return true 表示跳过过滤
     * @throws ServletException 解析处理器失败时抛出
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return request instanceof XssHttpServletRequestWrapper
                || isMultipartRequest(request)
                || isIgnoredRequest(request);
    }

    /**
     * 对请求进行 XSS 包装处理。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException I/O 异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        filterChain.doFilter(new XssHttpServletRequestWrapper(request, xssCleaner), response);
    }

    /**
     * 判断当前请求是否为 multipart 请求。
     *
     * @param request 当前请求
     * @return true 表示 multipart 请求
     */
    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    /**
     * 判断当前请求是否标记了 XSS 排除注解。
     *
     * @param request 当前请求
     * @return true 表示跳过 XSS 过滤
     * @throws ServletException 解析处理器失败时抛出
     */
    private boolean isIgnoredRequest(HttpServletRequest request) throws ServletException {
        HandlerExecutionChain handlerExecutionChain = resolveHandlerExecutionChain(request);
        if (handlerExecutionChain == null) {
            return false;
        }
        Object handler = handlerExecutionChain.getHandler();
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), XssIgnore.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), XssIgnore.class);
    }

    /**
     * 解析当前请求命中的处理器链。
     *
     * @param request 当前请求
     * @return 处理器执行链
     * @throws ServletException 解析失败时抛出
     */
    private HandlerExecutionChain resolveHandlerExecutionChain(HttpServletRequest request) throws ServletException {
        try {
            return handlerMapping.getHandler(request);
        } catch (Exception exception) {
            throw new ServletException("解析请求处理器失败", exception);
        }
    }
}
