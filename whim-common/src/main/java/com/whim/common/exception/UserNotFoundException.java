package com.whim.common.exception;

/**
 * @author Jince
 * date: 2024/10/21 23:21
 * description: 用户不存在异常
 */
public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}