package com.whim.log.aspect;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.json.utils.JsonUtils;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogStatus;
import com.whim.log.event.OperLogEvent;
import com.whim.web.model.Result;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 采集已标注的业务操作并发布事件，保持原有返回值和异常。
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogAspect {
    private static final Pattern FIELD_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
            "password|passwd|token|secret|credential|privatekey|accesskey|authorization|cookie|captcha",
            Pattern.CASE_INSENSITIVE);

    private final AuthenticationContext authenticationContext;
    private final ApplicationEventPublisher eventPublisher;

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
            if (authenticationContext.isLogin()) {
                event.operName(authenticationContext.getCurrentUserInfo().getUsername());
            }
            HttpServletRequest request = ServletUtils.getRequest();
            if (request != null) {
                event.requestMethod(request.getMethod()).requestUrl(StringUtils.left(request.getRequestURI(), 512))
                        .operIp(IPUtils.getClientIpAddress(request));
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            event.methodName(signature.getDeclaringTypeName() + "." + signature.getName());
            event.requestParam(getRequestParameters(signature, joinPoint.getArgs(), operation.requestFields()));
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
            try {
                event.costTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
                boolean success = failure == null;
                if (failure != null) {
                    event.errorMessage(getFailureReason(failure));
                } else if (response instanceof Result<?> result) {
                    success = result.isSuccess();
                    // 响应仅保留状态摘要，避免将用户资料、文件或令牌复制到审计表。
                    event.responseParam(JsonUtils.toJson(Map.of("code", result.getCode(), "success", success)));
                    if (!success) {
                        event.errorMessage("操作失败，业务状态码为" + result.getCode());
                    }
                }
                eventPublisher.publishEvent(event.status(success ? LogStatus.SUCCESS : LogStatus.FAILURE).build());
            } catch (RuntimeException exception) {
                log.error("操作日志事件投递失败，模块={}", operation.title(), exception);
            }
        }
    }

    /**
     * 保存项目定义的业务拒绝原因，底层异常只保留类别以免泄露技术细节。
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
        return "操作失败（" + failure.getClass().getSimpleName() + "）";
    }

    /**
     * 按注解声明的字段采集简单值，批量标识仅保留有限条数及总数。
     *
     * @param signature 控制器方法签名
     * @param arguments 已绑定的请求参数
     * @param fields 允许写入日志的字段
     * @return 请求摘要的 JSON，无匹配字段时为空
     */
    private String getRequestParameters(MethodSignature signature, Object[] arguments, String[] fields) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        String[] names = signature.getParameterNames();
        for (int index = 0; fields.length > 0 && index < arguments.length; index++) {
            Object argument = arguments[index];
            if (argument == null || argument instanceof ServletRequest || argument instanceof ServletResponse) {
                continue;
            }
            BeanWrapper properties = PropertyAccessorFactory.forBeanPropertyAccess(argument);
            for (String field : fields) {
                // 即使误配了注解，也禁止展开嵌套对象或读取凭据字段。
                if (!FIELD_NAME.matcher(field).matches() || SENSITIVE_FIELD.matcher(field).find()) {
                    continue;
                }
                Object value = null;
                if (names != null && field.equals(names[index])) {
                    value = argument;
                } else if (properties.isReadableProperty(field)) {
                    value = properties.getPropertyValue(field);
                }
                if (value instanceof String string) {
                    snapshot.put(field, StringUtils.left(string, 128));
                } else if (value instanceof Number || value instanceof Boolean) {
                    snapshot.put(field, value);
                } else if (value instanceof Collection<?> items) {
                    snapshot.put(field, items.stream().limit(50).filter(Number.class::isInstance).toList());
                    snapshot.put(field + "Count", items.size());
                }
            }
        }
        return snapshot.isEmpty() ? null : JsonUtils.toJson(snapshot);
    }
}
