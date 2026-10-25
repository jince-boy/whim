package com.whim.web.filter;

import com.whim.core.annotation.RequestDecrypt;
import com.whim.web.security.RequestEncryptionService;
import com.whim.web.wrapper.DecryptedRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 在 XSS 清洗前自动解密 JSON 密文，并对带注解的接口拒绝明文。
 */
@RequiredArgsConstructor
public class RequestDecryptionFilter extends OncePerRequestFilter {
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final RequestEncryptionService service;
    private final RequestMappingHandlerMapping handlerMapping;
    private final HandlerExceptionResolver exceptionResolver;

    /**
     * 解析目标接口并解密请求，解密异常交由项目全局异常处理器返回。
     *
     * @param request 原始请求
     * @param response 当前响应
     * @param filterChain 后续过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException 请求流读取失败
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        HandlerMethod handler = null;
        HttpServletRequest processed = request;
        try {
            var executionChain = handlerMapping.getHandler(request);
            if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod method) {
                handler = method;
                boolean encryptionRequired = method.hasMethodAnnotation(RequestDecrypt.class);
                boolean jsonBody = BODY_METHODS.contains(request.getMethod()) && request.getContentType() != null
                        && MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(request.getContentType()));
                if (encryptionRequired || jsonBody) {
                    processed = new DecryptedRequestWrapper(request, service.getRequestBody(request, encryptionRequired));
                }
            }
        } catch (Exception exception) {
            if (exceptionResolver.resolveException(request, response, handler, exception) == null) {
                throw new ServletException("请求解密处理失败", exception);
            }
            return;
        }
        filterChain.doFilter(processed, response);
    }
}
