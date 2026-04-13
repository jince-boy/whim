package com.whim.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.whim.core.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Jince
 * @date 2026/04/13
 * @description Sa-Token 全局异常处理，将框架异常转换为统一 {@link Result} 响应。
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
    public Result<Void> handleNotLoginException(NotLoginException exception) {
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
     * 处理缺少指定权限异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无权限异常
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return Result.permissionDenied("用户没有权限");
    }

    /**
     * 处理缺少指定角色异常，映射为 HTTP 403。
     *
     * @param exception Sa-Token 无角色异常
     * @return 无业务数据的失败响应
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return Result.permissionDenied("用户没有权限");
    }
}
