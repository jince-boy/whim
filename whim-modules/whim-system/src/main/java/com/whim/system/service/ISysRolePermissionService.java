package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.vo.role.RoleDataScopeVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysRolePermission业务服务。
 */
public interface ISysRolePermissionService extends IService<SysRolePermission> {
    /** 查询角色已分配权限ID。 */
    Set<Long> getPermissionIds(Long roleId);
    /** 覆盖角色权限，保留未改变操作的数据范围覆盖。 */
    void replacePermissions(Long roleId, Set<Long> permissionIds);
    /** 查询操作覆盖范围；为空表示继承角色默认范围。 */
    RoleDataScopeVO getPermissionDataScope(Long roleId, Long permissionId);
    /** 设置操作范围覆盖；dataScope为空表示恢复继承。 */
    void replacePermissionDataScope(Long roleId, Long permissionId, Integer dataScope, Set<Long> deptIds);
}

