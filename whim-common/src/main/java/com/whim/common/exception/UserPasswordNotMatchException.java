package com.whim.common.exception;

/**
 * @author Jince
 * date: 2024/10/21 23:22
 * description: 用户名或密码错误异常
 */
public final class UserPasswordNotMatchException extends RuntimeException {
    public UserPasswordNotMatchException(String message) {
        super(message);
    }
}