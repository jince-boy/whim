package com.whim.log.aspect;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogStatus;
import com.whim.log.event.OperLogEvent;
import com.whim.log.response.OperationLogResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 采集已标注的业务操作并发布事件，保持原有返回值和异常。
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogAspect {
    private final @Nullable AuthenticationContext authenticationContext;
    private final ApplicationEventPublisher eventPublisher;
    private final @Nullable OperationLogResponseHandler responseHandler;

    /**
     * 在请求线程采集操作者和结果，业务方法只执行一次。
     *
     * @param joinPoint 当前控制器调用
     * @param operation 操作审计配置
     * @return 控制器的原始返回值
     * @throws Throwable 控制器抛出的原始异常
     */
    @Around(value = "@annotation(operation)", argNames = "joinPoint,operation")
    public Object recordOperation(ProceedingJoinPoint joinPoint, Log operation) throws Throwable {
        OperLogEvent.OperLogEventBuilder event = OperLogEvent.builder().title(operation.title())
                .logType(operation.type()).operTime(LocalDateTime.now().withNano(0));
        try {
            // 密码变更或退出可能使当前会话失效，必须先保存操作者身份。
            if (authenticationContext != null && authenticationContext.isLogin()) {
                event.operName(authenticationContext.getCurrentUserInfo().getUsername());
            }
            HttpServletRequest request = ServletUtils.getRequest();
            if (request != null) {
                event.requestMethod(request.getMethod()).requestUrl(StringUtils.left(request.getRequestURI(), 512))
                        .operIp(IPUtils.getClientIpAddress(request));
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            event.methodName(signature.getDeclaringTypeName() + "." + signature.getName());
            event.requestParam(new OperationLogRequestSnapshot(operation.excludeFields())
                    .capture(signature.getMethod(), joinPoint.getArgs()));
        } catch (RuntimeException exception) {
            log.warn("操作日志来源信息采集失败，模块={}", operation.title(), exception);
        }

        long start = System.nanoTime();
        Object response = null;
        Throwable failure = null;
        try {
            response = joinPoint.proceed();
            return response;
        } catch (Throwable exception) {
            failure = exception;
            throw exception;
        } finally {
            event.costTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
                    .status(failure == null ? LogStatus.SUCCESS : LogStatus.FAILURE);
            boolean deferred = false;
            if (failure != null) {
                event.errorMessage(getFailureReason(failure));
                try {
                    // 控制器抛出异常时，MVC 尚未生成用户实际看到的错误响应。
                    deferred = responseHandler != null && responseHandler.deferFailureResponse(
                            result -> publishOperation(event, result));
                } catch (RuntimeException exception) {
                    log.warn("操作日志等待错误响应失败，模块={}", operation.title(), exception);
                }
            }
            if (!deferred) {
                publishOperation(event, response);
            }
        }
    }

    /**
     * 补充应用的公开响应并投递审计事件。
     *
     * @param event 已采集的操作信息
     * @param response 应用返回的响应，无响应时为空
     */
    private void publishOperation(OperLogEvent.OperLogEventBuilder event, @Nullable Object response) {
        try {
            if (response != null && responseHandler != null) {
                responseHandler.recordResponse(response, event);
            }
        } catch (RuntimeException exception) {
            log.warn("操作日志响应信息采集失败", exception);
        }
        OperLogEvent operation = event.build();
        try {
            eventPublisher.publishEvent(operation);
        } catch (RuntimeException exception) {
            log.error("操作日志事件投递失败，模块={}", operation.getTitle(), exception);
        }
    }

    /**
     * 在应用未提供错误响应时保留已知业务原因，其他失败使用通用说明。
     *
     * @param failure 操作抛出的异常
     * @return 可写入审计详情的失败原因
     */
    private String getFailureReason(Throwable failure) {
        if (failure.getCause() == null && (failure instanceof ServiceException
                || failure instanceof AccessDeniedException || failure instanceof DataAccessDeniedException)
                && StringUtils.isNotBlank(failure.getMessage())) {
            return StringUtils.left(failure.getMessage(), 512);
        }
        return "操作失败，未获取到具体原因";
    }
}
