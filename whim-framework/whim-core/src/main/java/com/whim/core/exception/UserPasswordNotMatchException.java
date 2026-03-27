package com.whim.core.exception;

/**
 * @author jince
 * @date 2026/3/27
 * @description 用户名或密码错误异常
 */
public final class UserPasswordNotMatchException extends RuntimeException {
    public UserPasswordNotMatchException(Throwable cause) {
        super(cause);
    }

    public UserPasswordNotMatchException(String message) {
        super(message);
    }

    public UserPasswordNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
