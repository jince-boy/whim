package com.whim.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/04/13
 * @description Sa-Token 全局异常处理，将框架异常转换为原生 HTTP 响应。
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 处理未登录异常，映射为 HTTP 401 与可读中文说明。
     *
     * @param exception Sa-Token 未登录异常
     * @return 无业务数据的失败响应，消息体为提示文案
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Map<String, Object>> handleNotLoginException(NotLoginException exception) {
        log.warn("认证异常:{}", exception.getMessage(), exception);
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
        return createResponse(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 处理缺少指定权限异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无权限异常
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Map<String, Object>> handleNotPermissionException(NotPermissionException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return createResponse(HttpStatus.FORBIDDEN, "用户没有权限");
    }

    /**
     * 处理缺少指定角色异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无角色异常
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Map<String, Object>> handleNotRoleException(NotRoleException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return createResponse(HttpStatus.FORBIDDEN, "用户没有权限");
    }

    /**
     * 创建与统一响应模型字段一致的原生 HTTP 响应。
     *
     * @param status  HTTP 状态码
     * @param message 响应消息
     * @return HTTP 响应实体
     */
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", status.value());
        responseBody.put("message", message);
        responseBody.put("data", null);
        responseBody.put("success", status.is2xxSuccessful());
        return ResponseEntity.status(status).body(responseBody);
    }
}
