package com.whim.common.exception;

/**
 * @author Jince
 * date: 2024/10/21 23:03
 * description: 业务异常类
 */
public final class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
