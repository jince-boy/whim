package com.whim.core.exception;

/**
 * @author jince
 * @date 2026/3/27
 * @description 用户不存在异常
 */
public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
