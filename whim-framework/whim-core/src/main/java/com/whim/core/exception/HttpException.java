package com.whim.core.exception;

/**
 * @author jince
 * date: 2025/8/22 16:21
 * description: http异常
 */
public final class HttpException extends RuntimeException {
    public HttpException(Throwable cause) {
        super(cause);
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
