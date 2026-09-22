package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 用户名不存在或密码错误异常
 */
public final class UserPasswordNotMatchException extends RuntimeException {

    /**
     * 根据原始异常创建认证失败异常。
     *
     * @param cause 原始异常
     */
    public UserPasswordNotMatchException(Throwable cause) {
        super(cause);
    }

    /**
     * 根据错误信息创建认证失败异常。
     *
     * @param message 错误信息
     */
    public UserPasswordNotMatchException(String message) {
        super(message);
    }

    /**
     * 根据错误信息和原始异常创建认证失败异常。
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public UserPasswordNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
