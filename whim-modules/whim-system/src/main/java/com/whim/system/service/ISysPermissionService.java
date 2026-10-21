package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysPermission;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号的有效操作权限查询。
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /** 按精确权限码查询有效页面或按钮的完整权限定义。 */
    SysPermission getPermissionByCode(String permissionCode);

    /** 根据登录时已核验的有效角色，构建权限码与授权角色的对应关系。 */
    Map<String, Set<Long>> getPermissionRoleIds(List<RoleInfo> roles);
}
