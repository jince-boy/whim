package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 当前身份未满足操作授权条件，与具体认证框架无关。
 */
public final class AccessDeniedException extends RuntimeException {

    /** 使用明确的访问拒绝原因创建异常。 */
    public AccessDeniedException(String message) {
        super(message);
    }

    /** 保留访问拒绝原因和原始异常，供框架适配及问题排查使用。 */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
