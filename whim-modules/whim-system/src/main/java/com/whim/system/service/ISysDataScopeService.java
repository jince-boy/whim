package com.whim.system.service;

import com.whim.core.permission.DataPermissionProvider;
import com.whim.core.permission.DataPermissionScope;

/**
 * @author Jince
 * @date 2026/10/16
 * @description 系统账号的操作级数据范围解析。
 */
public interface ISysDataScopeService extends DataPermissionProvider {
    /** 从登录上下文校验精确操作授权，合并对应角色的数据范围。 */
    @Override
    DataPermissionScope resolve(String permissionCode);
}
