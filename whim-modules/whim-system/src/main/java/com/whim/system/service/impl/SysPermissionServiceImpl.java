package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.model.dto.permission.RolePermissionDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.enums.SysMenuType;
import com.whim.system.service.ISysPermissionService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 查询登录账号的有效操作权限。
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
    /** 按精确权限码查询有效权限，逻辑删除条件由 MyBatis-Plus 自动追加。 */
    @Override
    public SysPermission getPermissionByCode(String permissionCode) {
        return lambdaQuery()
                .eq(SysPermission::getPerms, permissionCode)
                .eq(SysPermission::getStatus, 0)
                .in(SysPermission::getMenuType, SysMenuType.PAGE.getCode(), SysMenuType.BUTTON.getCode())
                .one();
    }

    /** 登录时一次性加载角色权限关系，供功能权限和数据权限共用。 */
    @Override
    public Map<String, Set<Long>> getPermissionRoleIds(List<RoleInfo> roles) {
        if (roles.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds = new HashSet<>();
        Long superAdministratorRoleId = null;
        for (RoleInfo role : roles) {
            roleIds.add(role.getRoleId());
            if ("superadmin".equals(role.getRoleCode())) {
                superAdministratorRoleId = role.getRoleId();
            }
        }
        Map<String, Set<Long>> permissionRoleIds = new LinkedHashMap<>();
        if (superAdministratorRoleId != null) {
            List<SysPermission> permissions = lambdaQuery().eq(SysPermission::getStatus, 0)
                    .in(SysPermission::getMenuType, SysMenuType.PAGE.getCode(), SysMenuType.BUTTON.getCode())
                    .isNotNull(SysPermission::getPerms).ne(SysPermission::getPerms, "").list();
            for (SysPermission permission : permissions) {
                permissionRoleIds.put(permission.getPerms(), Set.of(superAdministratorRoleId));
            }
            return permissionRoleIds;
        }
        for (RolePermissionDTO permission : baseMapper.selectRolePermissions(roleIds)) {
            permissionRoleIds.computeIfAbsent(permission.getPermissionCode(), key -> new LinkedHashSet<>())
                    .add(permission.getRoleId());
        }
        return permissionRoleIds;
    }
}
