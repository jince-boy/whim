package com.whim.web.handler;

import com.whim.core.exception.FileStorageException;
import com.whim.core.exception.HttpException;
import com.whim.core.exception.ServiceException;
import com.whim.core.exception.UserDisableException;
import com.whim.core.exception.UserNotFoundException;
import com.whim.core.exception.UserPasswordNotMatchException;
import com.whim.core.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Optional;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理未捕获的系统异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneralException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception on [{} {}]", request.getMethod(), request.getRequestURI(), exception);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部异常，请稍后重试").toResponseEntity();
    }

    /**
     * 处理不支持的请求方法异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        var supportedMethods = exception.getSupportedMethods();
        log.warn(
                "Unsupported request method on [{} {}], supported methods: {}",
                request.getMethod(),
                request.getRequestURI(),
                supportedMethods == null || supportedMethods.length == 0 ? "unknown" : String.join(", ", supportedMethods),
                exception
        );
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED, "请求方式不受支持").toResponseEntity();
    }

    /**
     * 处理资源不存在异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        log.warn("Resource not found on [{} {}]", request.getMethod(), request.getRequestURI(), exception);
        return Result.error(HttpStatus.NOT_FOUND, "请求资源不存在").toResponseEntity();
    }

    /**
     * 处理非法参数异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(
                HttpStatus.BAD_REQUEST,
                StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "请求参数非法"
        );
    }

    /**
     * 处理请求参数类型不匹配异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<List<Result.ValidationError>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        var requiredType = Optional.ofNullable(exception.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("unknown");
        var message = "请求参数类型不匹配，参数[%s]需要 %s 类型，当前值为 %s".formatted(
                exception.getName(),
                requiredType,
                String.valueOf(exception.getValue())
        );
        var errors = List.of(Result.fieldError(exception.getName(), message));

        log.warn("Argument type mismatch on [{} {}]: {}", request.getMethod(), request.getRequestURI(), message, exception);
        return Result.validationError("请求参数类型不匹配", errors);
    }

    /**
     * 处理请求体参数校验异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<List<Result.ValidationError>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        var errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return Result.fieldError(
                                fieldError.getField(),
                                StringUtils.hasText(fieldError.getDefaultMessage()) ? fieldError.getDefaultMessage() : "字段校验失败"
                        );
                    }
                    return Result.fieldError(
                            error.getObjectName(),
                            StringUtils.hasText(error.getDefaultMessage()) ? error.getDefaultMessage() : "对象校验失败"
                    );
                })
                .distinct()
                .toList();
        var summary = errors.stream()
                .map(error -> "%s=%s".formatted(error.getField(), error.getMessage()))
                .reduce((left, right) -> left + "; " + right)
                .orElse("unknown validation error");

        log.warn("Request body validation failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), summary);
        return Result.validationError("参数校验失败", errors);
    }

    /**
     * 处理表单绑定校验异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(BindException.class)
    public Result<List<Result.ValidationError>> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        var errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return Result.fieldError(
                                fieldError.getField(),
                                StringUtils.hasText(fieldError.getDefaultMessage()) ? fieldError.getDefaultMessage() : "字段校验失败"
                        );
                    }
                    return Result.fieldError(
                            error.getObjectName(),
                            StringUtils.hasText(error.getDefaultMessage()) ? error.getDefaultMessage() : "对象校验失败"
                    );
                })
                .distinct()
                .toList();
        var summary = errors.stream()
                .map(error -> "%s=%s".formatted(error.getField(), error.getMessage()))
                .reduce((left, right) -> left + "; " + right)
                .orElse("unknown validation error");

        log.warn("Binding validation failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), summary);
        return Result.validationError("参数校验失败", errors);
    }

    /**
     * 处理方法级约束校验异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<List<Result.ValidationError>> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        var errors = exception.getConstraintViolations().stream()
                .map(violation -> {
                    String field = null;
                    for (var node : violation.getPropertyPath()) {
                        if (StringUtils.hasText(node.getName())) {
                            field = node.getName();
                        }
                    }
                    return Result.fieldError(
                            StringUtils.hasText(field) ? field : "parameter",
                            StringUtils.hasText(violation.getMessage()) ? violation.getMessage() : "参数校验失败"
                    );
                })
                .distinct()
                .toList();
        var summary = errors.stream()
                .map(error -> "%s=%s".formatted(error.getField(), error.getMessage()))
                .reduce((left, right) -> left + "; " + right)
                .orElse("unknown validation error");

        log.warn("Constraint validation failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), summary);
        return Result.validationError("参数校验失败", errors);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<List<Result.ValidationError>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        var message = "缺少必要的请求参数";
        var errors = List.of(Result.fieldError(exception.getParameterName(), message));

        log.warn("Missing request parameter on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.validationError(message, errors);
    }

    /**
     * 处理请求绑定异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public Result<Void> handleServletRequestBindingException(
            ServletRequestBindingException exception,
            HttpServletRequest request
    ) {
        log.warn("Servlet request binding failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(HttpStatus.BAD_REQUEST, "请求头或绑定信息缺失");
    }

    /**
     * 处理不可读的请求体异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Unreadable request body on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(HttpStatus.BAD_REQUEST, "请求体格式错误或缺少必要字段").toResponseEntity();
    }

    /**
     * 处理远程 HTTP 调用异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(HttpException.class)
    public Result<Void> handleHttpException(HttpException exception, HttpServletRequest request) {
        log.error("Remote http invocation failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(HttpStatus.BAD_GATEWAY, "远程服务调用失败");
    }

    /**
     * 处理业务异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException exception, HttpServletRequest request) {
        log.error("Service exception on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "业务处理失败"
        );
    }

    /**
     * 处理用户不存在异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Result<Void> handleUserNotFoundException(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        log.warn("User not found on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(
                HttpStatus.NOT_FOUND,
                StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "用户不存在"
        );
    }

    /**
     * 处理用户名或密码错误异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(UserPasswordNotMatchException.class)
    public Result<Void> handleUserPasswordNotMatchException(
            UserPasswordNotMatchException exception,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.unauthorized(StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "用户名或密码错误");
    }

    /**
     * 处理用户禁用异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(UserDisableException.class)
    public Result<Void> handleUserDisableException(UserDisableException exception, HttpServletRequest request) {
        log.warn("Disabled user access on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.permissionDenied(StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "用户已被禁用");
    }

    /**
     * 处理文件存储异常
     *
     * @param exception 异常对象
     * @param request   当前请求
     * @return 统一错误响应
     */
    @ExceptionHandler(FileStorageException.class)
    public Result<Void> handleFileStorageException(FileStorageException exception, HttpServletRequest request) {
        log.error("File storage exception on [{} {}]: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        return Result.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                StringUtils.hasText(exception.getMessage()) ? exception.getMessage() : "文件存储失败"
        );
    }
}