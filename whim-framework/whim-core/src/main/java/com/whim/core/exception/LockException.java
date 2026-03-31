package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/03/31
 * @description 分布式锁获取失败异常
 */
public final class LockException extends RuntimeException {
    public LockException(Throwable cause) {
        super(cause);
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
