package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 当前请求缺少有效认证身份，与具体认证框架无关。
 */
public final class UnauthenticatedException extends RuntimeException {

    /** 使用明确的认证失败原因创建异常。 */
    public UnauthenticatedException(String message) {
        super(message);
    }

    /** 保留认证失败原因和原始异常，供框架适配及问题排查使用。 */
    public UnauthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
