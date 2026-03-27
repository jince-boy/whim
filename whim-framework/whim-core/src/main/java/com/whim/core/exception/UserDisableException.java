package com.whim.core.exception;

/**
 * @author jince
 * @date 2026/3/27
 * @description 用户被禁用异常
 */
public final class UserDisableException extends RuntimeException {
    public UserDisableException(Throwable cause) {
        super(cause);
    }

    public UserDisableException(String message) {
        super(message);
    }

    public UserDisableException(String message, Throwable cause) {
        super(message, cause);
    }
}
