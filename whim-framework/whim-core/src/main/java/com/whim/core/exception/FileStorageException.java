package com.whim.core.exception;

/**
 * @author Jince
 * @date 2024/11/15 22:40
 * @description 文件存储异常
 */
public final class FileStorageException extends RuntimeException {
    public FileStorageException(Throwable cause) {
        super(cause);
    }

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
