package com.whim.core.exception;

/**
 * @author jince
 * date: 2025/8/16 19:47
 * description: excel异常
 */
public final class ExcelException extends RuntimeException {
    public ExcelException(String message) {
        super(message);
    }
    public ExcelException(Throwable cause) {
        super(cause);
    }
    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
