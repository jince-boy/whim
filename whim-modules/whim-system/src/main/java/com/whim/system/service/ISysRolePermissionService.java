package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysRolePermission;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色权限关联表服务接口
 */
public interface ISysRolePermissionService extends IService<SysRolePermission> {
    /** 查询当前租户角色已分配的权限ID。 */
    Set<Long> getCurrentTenantPermissionIds(Long roleId);

    /** 覆盖当前租户角色的权限。 */
    void replaceCurrentTenantPermissions(Long roleId, Set<Long> permissionIds);
}

