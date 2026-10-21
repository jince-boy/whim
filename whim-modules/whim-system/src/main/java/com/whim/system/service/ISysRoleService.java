package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysRole;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号的有效角色查询。
 */
public interface ISysRoleService extends IService<SysRole> {
    /** 查询账号的有效角色。 */
    List<RoleInfo> getRoleInfoListByUserId(Long userId);
}
