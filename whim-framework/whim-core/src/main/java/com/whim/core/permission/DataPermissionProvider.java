package com.whim.core.permission;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 业务模块提供的操作级数据权限解析契约。
 */
public interface DataPermissionProvider {
    /** 校验当前身份的操作授权，返回该操作的有效数据范围；缺少授权时拒绝。 */
    DataPermissionScope resolve(String permissionCode);
}
