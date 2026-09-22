package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 当前用户无权访问目标租户时抛出的异常。
 */
public final class TenantAccessDeniedException extends RuntimeException {

    /**
     * 使用指定错误信息创建异常。
     *
     * @param message 错误信息
     */
    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
