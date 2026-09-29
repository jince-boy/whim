package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 请求次数超过限制异常
 */
public final class TooManyRequestsException extends RuntimeException {

    /**
     * 创建请求过于频繁异常。
     *
     * @param message 错误信息
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}
