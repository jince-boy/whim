package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysUserRole;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysUserRole业务服务。
 */
public interface ISysUserRoleService extends IService<SysUserRole> {
    /** 查询本次操作可见用户的角色ID。 */
    Set<Long> getRoleIds(Long userId);
    /** 校验用户与可委派角色后覆盖关联，并撤销旧会话。 */
    void replaceRoles(Long userId, Set<Long> roleIds);
}

