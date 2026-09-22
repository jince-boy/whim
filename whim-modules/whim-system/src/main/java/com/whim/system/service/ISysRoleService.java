package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色表服务接口
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * 查询用户已启用角色编码集合。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @return 角色编码集合
     */
    Set<String> getRoleCodeSetByUserIdAndTenantId(Long userId, Long tenantId);

    /**
     * 查询用户已启用角色的完整信息。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @return 角色信息列表
     */
    List<RoleInfo> getRoleInfoListByUserIdAndTenantId(Long userId, Long tenantId);

    /**
     * 判断用户是否拥有全局超级管理员角色。
     *
     * @param userId 用户ID
     * @return true 表示拥有超级管理员角色
     */
    boolean isSuperAdministrator(Long userId);
}

