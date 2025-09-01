package com.whim.log.aspect;

import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.core.utils.SpringUtils;
import com.whim.json.utils.JsonUtils;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogStatus;
import com.whim.log.event.OperLogEvent;
import com.whim.satoken.core.logic.StpAuthManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/8/18 17:26
 * description: 日志切面
 */
@Slf4j
@Aspect
@AutoConfiguration
public class LogAspect {

    /**
     * 默认需要脱敏的字段
     */
    private static final Set<String> DEFAULT_EXCLUDE_FIELDS = Set.of(
            "password", "oldPassword", "newPassword", "confirmPassword"
    );

    @Around("@annotation(controllerLog)")
    public Object around(ProceedingJoinPoint joinPoint, Log controllerLog) throws Throwable {
        long startTime = System.nanoTime();
        OperLogEvent operLogEvent = new OperLogEvent();

        try {


            // 1. 基础上下文信息
            operLogEvent.setOperTime(LocalDateTime.now());
            operLogEvent.setOperIp(IPUtils.getClientIpAddress());
            operLogEvent.setOperLocation(IPUtils.getCityInfo());
            operLogEvent.setRequestUrl(Objects.requireNonNull(ServletUtils.getRequest()).getRequestURI());
            operLogEvent.setRequestMethod(Objects.requireNonNull(ServletUtils.getRequest()).getMethod());
            operLogEvent.setOperName(StpAuthManager.SYSTEM.getExtra("username").toString());

            // 2. 方法 & 注解元数据
            operLogEvent.setMethodName(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
            operLogEvent.setLogType(controllerLog.logType().getCode());
            operLogEvent.setTitle(controllerLog.title());

            // 3. 请求参数
            if (controllerLog.isSaveRequestData()) {
                String params = extractRequestParams(joinPoint, controllerLog.excludeParamNames());
                operLogEvent.setRequestParam(params);
            }

            // 4. 执行业务方法
            Object result = joinPoint.proceed();

            // 5. 返回值
            if (controllerLog.isSaveResponseData() && result != null) {
                operLogEvent.setResponseParam(JsonUtils.toJsonString(result));
            }

            operLogEvent.setStatus(LogStatus.SUCCESS.getCode());

            return result;
        } catch (Exception ex) {
            operLogEvent.setStatus(LogStatus.FAILURE.getCode());
            operLogEvent.setErrorMessage(ex.getMessage());
            throw ex;
        } finally {
            long constTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            operLogEvent.setCostTime(constTime);

            SpringUtils.getApplicationContext().publishEvent(operLogEvent);
        }
    }

    /**
     * 提取请求参数
     */
    private String extractRequestParams(ProceedingJoinPoint joinPoint, String[] excludeFields) {
        Map<String, String> paramMap = ServletUtils.getParameterAll();
        String method = Objects.requireNonNull(ServletUtils.getRequest()).getMethod();
        if (paramMap.isEmpty() && !HttpMethod.GET.matches(method)) {
            return argsToJson(joinPoint, excludeFields);
        } else {
            paramMap.keySet().removeIf(key -> shouldExclude(key, excludeFields));
            return paramMap.isEmpty() ? null : JsonUtils.toJsonString(paramMap);
        }
    }

    /**
     * 方法参数转 JSON，排除敏感字段
     */
    private String argsToJson(ProceedingJoinPoint joinPoint, String[] excludeFields) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Map<String, Object> merged = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (isFiltered(arg)) {
                continue;
            }
            String paramName = (paramNames != null && i < paramNames.length) ? paramNames[i] : "arg" + i;
            Map<String, Object> map;
            try {
                map = JsonUtils.parseMap(JsonUtils.toJsonString(arg));
            } catch (Exception e) {
                map = null;
            }
            if (map != null && !map.isEmpty()) {
                map.keySet().removeIf(key -> Set.of("password", "oldPassword", "newPassword", "confirmPassword")
                        .contains(key) || Arrays.asList(excludeFields).contains(key));
                merged.putAll(map);
            } else {
                merged.put(paramName, arg);
            }
        }

        return merged.isEmpty() ? null : JsonUtils.toJsonString(merged);
    }


    /**
     * 判断是否需要过滤的对象类型
     */
    private boolean isFiltered(Object obj) {
        return obj instanceof MultipartFile ||
                obj instanceof HttpServletRequest ||
                obj instanceof HttpServletResponse ||
                obj instanceof BindingResult;
    }

    /**
     * 判断字段是否需要排除
     */
    private boolean shouldExclude(String field, String[] excludeFields) {
        return DEFAULT_EXCLUDE_FIELDS.contains(field) ||
                Arrays.asList(excludeFields).contains(field);
    }
}
