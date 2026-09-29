package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.model.vo.role.RoleDataScopeVO;

import java.util.Set;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色表服务接口
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * 查询用户已启用角色的完整信息。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @return 角色信息列表
     */
    List<RoleInfo> getRoleInfoListByUserIdAndTenantId(Long userId, Long tenantId);

    /** 查询本次操作实际授权的租户角色；没有授权时返回空列表。 */
    List<RoleInfo> getAuthorizedDataScopeRoles(Long userId, Long tenantId, String permissionCode);

    /** 按本次功能权限码计算当前租户的数据范围并集；空范围拒绝。 */
    DataScopeDecisionDTO resolveCurrentTenantDataScope(String permissionCode);

    /**
     * 判断用户是否拥有全局超级管理员角色。
     *
     * @param userId 用户ID
     * @return true 表示拥有超级管理员角色
     */
    boolean isSuperAdministrator(Long userId);

    /** 查询当前租户的角色。 */
    List<RoleVO> listCurrentTenantRoles();

    /** 查询角色并确认其属于指定租户。 */
    SysRole getRequiredTenantRole(Long roleId, Long tenantId);

    /** 创建当前租户角色。 */
    Long createCurrentTenantRole(RoleSaveDTO request);

    /** 修改当前租户角色。 */
    void updateCurrentTenantRole(Long roleId, RoleSaveDTO request);

    /** 修改当前租户角色状态。 */
    void setCurrentTenantRoleStatus(Long roleId, Integer status);

    /** 查询当前租户角色的数据范围与自定义部门。 */
    RoleDataScopeVO getCurrentTenantDataScope(Long roleId);

    /** 覆盖当前租户角色的数据范围与自定义部门。 */
    void replaceCurrentTenantDataScope(Long roleId, Integer dataScope, Set<Long> deptIds);
}

