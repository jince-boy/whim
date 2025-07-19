package com.whim.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.whim.core.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author jince
 * @date 2025/6/19 16:23
 * @description 认证异常处理
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {
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
     * 用户没有角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<String> handleNotRoleException(NotRoleException exception) {
        log.warn("权限不足:{}", exception.getMessage(), exception);
        return Result.permissionDenied("用户没有权限");
    }
}
