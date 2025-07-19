package com.whim.core.exception;

/**
 * @author jince
 * @date 2024/6/26 下午2:33
 * @description 验证码验证异常
 */
public final class CheckCaptchaException extends RuntimeException {
    public CheckCaptchaException(Throwable cause) {
        super(cause);
    }

    public CheckCaptchaException(String message) {
        super(message);
    }
}
