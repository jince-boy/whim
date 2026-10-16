package com.whim.system.service;

import com.whim.core.permission.DataPermissionProvider;
import com.whim.core.permission.DataPermissionScope;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.entity.SysUser;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/16
 * @description 系统账号的操作级数据范围解析。
 */
public interface ISysDataScopeService extends DataPermissionProvider {
    /** 校验当前身份及精确操作授权，合并该操作的有效角色范围。 */
    @Override
    DataPermissionScope resolve(String permissionCode);

    /** 按目标账号计算同一操作的可信授权记录，供角色委派校验使用。 */
    DataPermissionScope resolveGrants(SysUser user, List<DataScopeGrantDTO> grants);
}
