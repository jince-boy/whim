package com.whim.core.exception;

/**
 * @author jince
 * date: 2026/3/24 21:53
 * description: 业务异常类
 */
public final class ServiceException extends RuntimeException {
    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
