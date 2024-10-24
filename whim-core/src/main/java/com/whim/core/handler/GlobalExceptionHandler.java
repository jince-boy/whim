package com.whim.core.handler;

import com.whim.common.exception.ServiceException;
import com.whim.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Jince
 * date: 2024/10/21 23:24
 * description: 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//    /**
//     * 兜底异常处理器
//     */
//    @ExceptionHandler(Throwable.class)
//    public Result<String> handleGeneralException(Throwable e) {
//        log.error("发生了一个未预期的异常", e);
//        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后再试");
//    }
//    /**
//     * 非法参数异常
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
//        log.error("非法参数异常:{}", e.getMessage());
//        return Result.error(HttpStatus.BAD_REQUEST, e.getMessage());
//    }
//    /**
//     * 参数验证异常
//     */
//    @ExceptionHandler(BindException.class)
//    public Result<String> handleBindException(BindException exception) {
//        List<String> collect = exception.getBindingResult().getFieldErrors().stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.toList());
//        log.error(String.join("; ", collect));
//        return Result.error(StringUtils.join(collect, ";"));
//    }
//    /**
//     * 参数验证异常
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
//        List<String> collect = exception.getFieldErrors().stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.toList());
//        log.error(String.join("; ", collect));
//        return Result.error(StringUtils.join(collect, ";"));
//    }
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
        log.error("业务异常信息:{}",exception.getMessage());
        return Result.error(exception.getMessage());
    }
//    /**
//     * 用户未登录异常
//     */
//    @ExceptionHandler(NotLoginException.class)
//    public Result<String> handleNotLoginException(NotLoginException exception) {
//        log.error("用户未登录异常:{}",exception.getMessage());
//        return Result.unauthorized("用户未认证");
//    }
}
