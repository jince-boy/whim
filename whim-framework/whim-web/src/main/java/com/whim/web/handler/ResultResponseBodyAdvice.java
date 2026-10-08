package com.whim.web.handler;

import com.whim.web.model.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author Jince
 * @date 2026/10/01
 * @description 将统一响应中的状态码同步到 HTTP 响应。
 */
@RestControllerAdvice
public class ResultResponseBodyAdvice implements ResponseBodyAdvice<Result<?>> {

    /** 仅处理声明返回 Result 的接口和异常处理方法。 */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    /** 写出响应体前同步 HTTP 状态，保留原始响应内容。 */
    @Override
    public Result<?> beforeBodyWrite(Result<?> body, MethodParameter returnType, MediaType selectedContentType,
                                     Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                     ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null) {
            response.setStatusCode(HttpStatusCode.valueOf(body.getCode()));
        }
        return body;
    }
}
