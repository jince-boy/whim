package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysUserRole;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户角色关联表服务接口
 */
public interface ISysUserRoleService extends IService<SysUserRole> {
    /** 查询成员在当前租户的角色ID。 */
    Set<Long> getCurrentTenantRoleIds(Long userId);

    /** 覆盖成员在当前租户的角色。 */
    void replaceCurrentTenantRoles(Long userId, Set<Long> roleIds);
}

