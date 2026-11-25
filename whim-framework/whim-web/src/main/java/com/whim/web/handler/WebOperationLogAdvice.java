package com.whim.web.handler;

import com.whim.core.utils.IdUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.json.utils.JsonUtils;
import com.whim.log.enums.LogStatus;
import com.whim.log.event.OperLogEvent;
import com.whim.log.response.OperationLogResponseHandler;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 将 Web 模块的统一响应接入操作日志。
 */
@RestControllerAdvice
@ConditionalOnClass(OperationLogResponseHandler.class)
public class WebOperationLogAdvice implements OperationLogResponseHandler, ResponseBodyAdvice<Result<?>> {
    private static final String FAILURE_RESPONSE = WebOperationLogAdvice.class.getName() + ".failureResponse";

    /**
     * 保存统一响应的状态摘要，失败时记录接口向用户返回的原因。
     *
     * @param response 应用响应
     * @param event 操作日志快照构建器
     */
    @Override
    public void recordResponse(Object response, OperLogEvent.OperLogEventBuilder event) {
        if (response instanceof Result<?> result) {
            if (!result.isSuccess()) {
                String message = StringUtils.left(StringUtils.defaultIfBlank(result.getMessage(), "操作失败"), 512);
                event.status(LogStatus.FAILURE).errorMessage(message);
                event.responseParam(JsonUtils.toJson(Map.of("code", result.getCode(), "success", false, "message", message)));
            } else {
                event.responseParam(JsonUtils.toJson(Map.of("code", result.getCode(), "success", true)));
            }
        }
    }

    /**
     * 在本次 MVC 请求结束时用最终的错误响应投递日志，未产生响应体时仍保留失败记录。
     *
     * @param complete 完成审计并投递事件的回调
     * @return 是否已登记请求结束回调
     */
    @Override
    public boolean deferFailureResponse(Consumer<@Nullable Object> complete) {
        ServletRequestAttributes attributes = ServletUtils.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        HttpServletRequest request = attributes.getRequest();
        request.setAttribute(FAILURE_RESPONSE, Boolean.TRUE);
        attributes.registerDestructionCallback(FAILURE_RESPONSE + IdUtils.uuid32(), () -> {
            Object result = request.getAttribute(FAILURE_RESPONSE);
            complete.accept(result instanceof Result<?> ? result : null);
        }, RequestAttributes.SCOPE_REQUEST);
        return true;
    }

    /**
     * 仅观察应用的统一响应。
     *
     * @param returnType 响应声明类型
     * @param converterType 消息转换器类型
     * @return 是否处理此响应
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 将全局异常处理完成后的公开响应交给当前请求的操作日志。
     *
     * @param body 统一响应体
     * @param returnType 响应声明类型
     * @param selectedContentType 响应媒体类型
     * @param selectedConverterType 消息转换器类型
     * @param request 当前请求
     * @param response 当前响应
     * @return 原始响应体
     */
    @Override
    public @Nullable Result<?> beforeBodyWrite(@Nullable Result<?> body, MethodParameter returnType, MediaType selectedContentType,
                                               Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                               ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null && !body.isSuccess() && request instanceof ServletServerHttpRequest servletRequest
                && servletRequest.getServletRequest().getAttribute(FAILURE_RESPONSE) != null) {
            servletRequest.getServletRequest().setAttribute(FAILURE_RESPONSE, body);
        }
        return body;
    }
}
