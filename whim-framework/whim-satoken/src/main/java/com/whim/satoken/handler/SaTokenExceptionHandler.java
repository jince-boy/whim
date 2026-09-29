package com.whim.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Jince
 * @date 2026/04/13
 * @description Sa-Token 全局异常处理，将框架异常转换为项目统一响应。
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 处理未登录异常，映射为 HTTP 401 与可读中文说明。
     *
     * @param exception Sa-Token 未登录异常
     * @param request 当前请求
     * @return 无业务数据的失败响应，消息体为提示文案
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException exception,
                                                               HttpServletRequest request) {
        log.warn("请求 [{} {}] 认证失败，原因类型：{}",
                request.getMethod(), request.getRequestURI(), exception.getType());
        String message = switch (exception.getType()) {
            case NotLoginException.NOT_TOKEN -> "未能读取到有效 token";
            case NotLoginException.INVALID_TOKEN -> "token 无效";
            case NotLoginException.TOKEN_TIMEOUT -> "token 已过期";
            case NotLoginException.BE_REPLACED -> "token 已被顶下线";
            case NotLoginException.KICK_OUT -> "token 已被踢下线";
            case NotLoginException.TOKEN_FREEZE -> "token 已被冻结";
            case NotLoginException.NO_PREFIX -> "未按照指定前缀提交 token";
            default -> "当前会话未登录";
        };
        return Result.error(HttpStatus.UNAUTHORIZED, message).toResponseEntity();
    }

    /**
     * 处理缺少指定权限异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无权限异常
     * @param request 当前请求
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<Void>> handleNotPermissionException(NotPermissionException exception,
                                                                    HttpServletRequest request) {
        log.warn("请求 [{} {}] 缺少功能权限", request.getMethod(), request.getRequestURI());
        return Result.error(HttpStatus.FORBIDDEN, "用户没有权限").toResponseEntity();
    }

    /**
     * 处理缺少指定角色异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无角色异常
     * @param request 当前请求
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<Void>> handleNotRoleException(NotRoleException exception,
                                                              HttpServletRequest request) {
        log.warn("请求 [{} {}] 缺少所需角色", request.getMethod(), request.getRequestURI());
        return Result.error(HttpStatus.FORBIDDEN, "用户没有权限").toResponseEntity();
    }
}
