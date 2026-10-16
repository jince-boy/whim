package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.role.RoleDataScopeVO;
import com.whim.system.model.vo.role.RoleVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysRole业务服务。
 */
public interface ISysRoleService extends IService<SysRole> {
    /** 查询默认数据范围及自定义部门。 */
    RoleDataScopeVO getDataScope(Long roleId);
    /** 查询角色已分配权限ID。 */
    Set<Long> getPermissionIds(Long roleId);
    /** 查询操作覆盖范围；为空表示继承角色默认范围。 */
    RoleDataScopeVO getPermissionDataScope(Long roleId, Long permissionId);
    /** 查询账号的有效角色。 */
    List<RoleInfo> getRoleInfoListByUserId(Long userId);
    /** 查询系统角色目录。 */
    List<RoleVO> listRoles();
    /** 获取未删除角色，不返回跨域身份。 */
    SysRole getRequiredRole(Long roleId);
    /** 判断未删除账号是否持有超级管理员角色，包含停用账号。 */
    boolean hasSuperAdministratorRole(Long userId);
    /** 创建普通角色，默认仅本人范围。 */
    Long createRole(RoleSaveDTO request);
    /** 修改普通角色名称和编码。 */
    void updateRole(Long roleId, RoleSaveDTO request);
    /** 修改角色状态并撤销受影响会话。 */
    void setRoleStatus(Long roleId, Integer status);
    /** 覆盖默认数据范围及自定义部门。 */
    void replaceDataScope(Long roleId, Integer dataScope, Set<Long> deptIds);
    /** 覆盖角色权限，保留未改变操作的数据范围覆盖。 */
    void replacePermissions(Long roleId, Set<Long> permissionIds);
    /** 设置操作范围覆盖；dataScope为空表示恢复继承。 */
    void replacePermissionDataScope(Long roleId, Long permissionId, Integer dataScope, Set<Long> deptIds);
    /** 删除未分配给用户的普通角色。 */
    void deleteRole(Long roleId);
    /** 校验角色可修改，禁止修改内置超级管理员及本人持有角色的授权。 */
    void requireMutableRole(SysRole role);
    /** 校验候选角色授予目标账号的权限和数据范围不超出操作者。 */
    void requireDelegableRole(SysRole role, SysUser targetUser);
    /** 恢复账号或迁移部门前重新核验其全部有效角色的委派边界。 */
    void requireDelegableUserRoles(SysUser targetUser);
    /** 在调用方业务事务中锁定内置角色，串行保护管理员成员变更。 */
    void lockAdministratorMembership();
    /** 检查账号停用、删除或角色移除不会消灭最后一个有效超级管理员。 */
    void requireAdministratorCanBeRemoved(Long userId);
    /** 默认范围与操作覆盖范围共用参数及有效部门校验。 */
    void validateDataScope(Integer dataScope, Set<Long> deptIds);
}

