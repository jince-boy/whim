package com.whim.core.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.whim.common.exception.ServiceException;
import com.whim.common.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jince
 * date: 2024/10/21 23:24
 * description: 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 兜底异常处理器
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("请求地址:{},发生了一个未预期的异常", request.getRequestURI(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * 请求方式错误
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("请求地址:{},不支持'{}'请求", request.getRequestURI(), e.getMethod());
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED, "请求方式错误,不支持该请求方式");
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("404 找不到该资源:{}", e.getMessage());
        return Result.error(HttpStatus.NOT_FOUND, "找不到该资源");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常:{}", e.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<List<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
//        Class<?> zone = exception.getParameter().getParameterType();
//
//        List<String> fieldOrder = Arrays.stream(zone.getDeclaredFields()).map(Field::getName).toList();
//
//        List<Map<String, String>> collect = exception.getFieldErrors().stream()
//                .sorted(Comparator.comparingInt(fieldError -> fieldOrder.indexOf(fieldError.getField())))
//                .map(fieldError -> {
//                    Map<String, String> errorDetail = new HashMap<>();
//                    errorDetail.put("field", fieldError.getField());
//                    errorDetail.put("message", fieldError.getDefaultMessage());
//                    return errorDetail;
//                })
//                .toList();
        String message = exception.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }

    /**
     * 参数验证异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException exception) {
        List<String> collect = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        log.error(String.join("; ", collect));
        return Result.error(StringUtils.join(collect, ";"));
    }

//    /**
//     * 参数验证异常
//     */
//    @ExceptionHandler(ConstraintViolationException.class)
//    public Result<String> handleConstraintViolationException(ConstraintViolationException exception) {
//        List<String> collect = exception.getConstraintViolations().stream()
//                .map(ConstraintViolation::getMessage)
//                .collect(Collectors.toList());
//        log.error(String.join("; ", collect));
//        return Result.error(StringUtils.join(collect, ";"));
//    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler(ServiceException.class)
    public Result<String> handleServiceExceptionHandler(ServiceException exception) {
        log.error("业务异常信息:{}", exception.getMessage());
        return Result.error(exception.getMessage());
    }

    /**
     * 用户未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException exception) {
        log.error("认证异常:{}", exception.getMessage());
        return Result.unauthorized("用户未认证");
    }
}
