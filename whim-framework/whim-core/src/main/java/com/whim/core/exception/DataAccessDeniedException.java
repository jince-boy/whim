package com.whim.core.exception;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 当前操作缺少功能授权或数据范围授权。
 */
public final class DataAccessDeniedException extends RuntimeException {
    /** 使用明确的拒绝原因创建异常。 */
    public DataAccessDeniedException(String message) {
        super(message);
    }
}
