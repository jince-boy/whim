package com.whim.core.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.whim.common.exception.CheckCaptchaException;
import com.whim.common.exception.FileStorageException;
import com.whim.common.exception.ServiceException;
import com.whim.common.exception.UserNotFoundException;
import com.whim.common.exception.UserPasswordNotMatchException;
import com.whim.common.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("请求地址:{},发生了一个未预期的异常", request.getRequestURI(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR, "服务器异常,请稍后重试,或联系业务人员处理.");
    }

    /**
     * 请求方式错误异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        log.warn("请求地址:{},不支持'{}'请求", request.getRequestURI(), exception.getMethod(), exception);
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED, "请求方式错误,不支持该请求方式");
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn("404 找不到该资源:{}", exception.getMessage(), exception);
        return Result.error(HttpStatus.NOT_FOUND, "找不到该资源");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("非法参数异常:{}", exception.getMessage(), exception);
        return Result.error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * 处理方法参数验证异常。
     * <p>
     * 当使用 @Valid 注解进行参数验证时，如果参数不符合要求，将抛出此异常。
     * 通常用于控制器方法参数的验证，适用于 Spring MVC 的请求体和请求参数。
     * </p>
     *
     * @param exception MethodArgumentNotValidException 异常对象
     * @return 包含错误信息的 Result 对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<List<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "error", Objects.requireNonNull(fieldError.getDefaultMessage())
                ))
                .collect(Collectors.toList());
        return Result.validationError("参数验证错误", errors);
    }

    /**
     * 处理绑定异常。
     * <p>
     * 当请求参数绑定到 Java 对象时，如果存在验证失败的情况，将抛出此异常。
     * 适用于 Spring MVC 中的表单提交和数据绑定场景。
     * </p>
     *
     * @param exception BindException 异常对象
     * @return 包含错误信息的 Result 对象
     */
    @ExceptionHandler(BindException.class)
    public Result<List<Map<String, String>>> handleBindException(BindException exception) {
        List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "error", Objects.requireNonNull(fieldError.getDefaultMessage())
                ))
                .collect(Collectors.toList());
        return Result.validationError("参数验证错误", errors);
    }

    /**
     * 处理约束验证异常。
     * <p>
     * 当使用 JSR-303/JSR-380 注解（如 @NotNull、@Size 等）进行字段验证时，如果字段不符合约束条件，将抛出此异常。
     * 适用于在服务层或持久层对实体类进行验证时。
     * </p>
     *
     * @param exception ConstraintViolationException 异常对象
     * @return 包含错误信息的 Result 对象
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<List<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException exception) {
        List<Map<String, String>> errors = exception.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "field", violation.getPropertyPath().toString(),
                        "error", violation.getMessage()
                ))
                .collect(Collectors.toList());
        return Result.validationError("参数验证错误", errors);
    }

    /**
     * HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("HTTP消息不可读异常: {}", exception.getMessage(), exception);
        return Result.error(HttpStatus.BAD_REQUEST, "请求体格式错误或缺失");
    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler(ServiceException.class)
    public Result<String> handleServiceExceptionHandler(ServiceException exception) {
        log.error("业务异常信息:{}", exception.getMessage(), exception);
        return Result.error(exception.getMessage());
    }

    /**
     * 验证码错误异常
     */
    @ExceptionHandler(CheckCaptchaException.class)
    public Result<String> handleCheckCaptchaException(CheckCaptchaException exception) {
        log.warn(exception.getMessage(), exception);
        return Result.validationError(exception.getMessage());
    }

    /**
     * 用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Result<String> handleUserNotFoundException(UserNotFoundException exception) {
        log.warn(exception.getMessage(), exception);
        return Result.error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * 用户名或密码错误异常
     */
    @ExceptionHandler(UserPasswordNotMatchException.class)
    public Result<String> handleUserPasswordNotMatchException(UserPasswordNotMatchException exception) {
        log.warn(exception.getMessage(), exception);
        return Result.unauthorized(exception.getMessage());
    }

    /**
     * 用户未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handleNotLoginException(NotLoginException exception) {
        log.warn("认证异常:{}", exception.getMessage(), exception);
        // 判断场景值，定制化异常信息
        String message = "";
        if (exception.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未能读取到有效 token";
        } else if (exception.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "token 无效";
        } else if (exception.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token 已过期";
        } else if (exception.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token 已被顶下线";
        } else if (exception.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token 已被踢下线";
        } else if (exception.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "token 已被冻结";
        } else if (exception.getType().equals(NotLoginException.NO_PREFIX)) {
            message = "未按照指定前缀提交 token";
        } else {
            message = "当前会话未登录";
        }
        return Result.unauthorized(message);
    }

    /**
     * 用户没有权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<String> handleNotPermissionException(NotPermissionException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return Result.permissionDenied("用户没有权限");
    }

    /**
     * 文件存储异常
     */
    @ExceptionHandler(FileStorageException.class)
    public Result<String> handleFileStorageException(FileStorageException exception) {
        log.error("文件存储异常:{}", exception.getMessage(), exception);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }


}
