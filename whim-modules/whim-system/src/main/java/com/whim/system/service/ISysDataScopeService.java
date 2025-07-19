package com.whim.system.service;

/**
 * @author jince
 * @date 2025/7/10 16:05
 * @description 系统数据权限接口
 */
public interface ISysDataScopeService {
    /**
     * 获取角色自定义数据权限
     * @param roleId 角色id
     * @return 自定义数据权限
     */
    String getRoleCustomDataPermission(Long roleId);

    /**
     * 获取部门及子部门
     * @param deptId 部门id
     * @return 部门及子部门
     */
    String getDeptAndChildDept(Long deptId);
}
