package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysRolePermissionMapper;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysTenantPackagePermission;
import com.whim.system.service.AuthorizationSessionInvalidator;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysTenantPackagePermissionService;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色权限关联表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {
    private final ISysTenantService tenantService;
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final ISysTenantPackagePermissionService packagePermissionService;
    private final SysUserRoleMapper userRoleMapper;
    private final AuthenticationContext authenticationContext;
    private final AuthorizationSessionInvalidator sessionInvalidator;

    /** 查询当前租户角色已分配的权限ID。 */
    @Override
    public Set<Long> getCurrentTenantPermissionIds(Long roleId) {
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        roleService.getRequiredTenantRole(roleId, tenantId);
        Set<Long> permissionIds = new LinkedHashSet<>();
        for (SysRolePermission binding : lambdaQuery().eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getTenantId, tenantId).list()) {
            permissionIds.add(binding.getPermissionId());
        }
        return permissionIds;
    }

    /** 覆盖当前租户角色的权限并在提交后撤销旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCurrentTenantPermissions(Long roleId, Set<Long> permissionIds) {
        SysTenant tenant = tenantService.getRequiredCurrentTenant();
        SysRole role = roleService.getRequiredTenantRole(roleId, tenant.getId());
        if (role.getStatus() != 0) {
            throw new IllegalArgumentException("不能修改已停用角色的权限");
        }
        Set<Long> allowedIds = new HashSet<>();
        for (SysTenantPackagePermission binding : packagePermissionService.lambdaQuery()
                .eq(SysTenantPackagePermission::getPackageId, tenant.getPackageId()).list()) {
            allowedIds.add(binding.getPermissionId());
        }
        if (!allowedIds.containsAll(permissionIds)) {
            throw new TenantAccessDeniedException("角色权限超出当前租户套餐范围");
        }
        if (!permissionIds.isEmpty()) {
            List<SysPermission> permissions = permissionService.listByIds(permissionIds);
            if (permissions.size() != permissionIds.size()
                    || permissions.stream().anyMatch(permission -> permission.getStatus() != 0
                    || permission.getPerms().startsWith("system:platform:"))) {
                throw new IllegalArgumentException("权限不存在、已停用或属于平台范围");
            }
        }
        List<SysRolePermission> existing = lambdaQuery().eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getTenantId, tenant.getId()).list();
        List<Long> removedIds = existing.stream()
                .filter(binding -> !permissionIds.contains(binding.getPermissionId()))
                .map(SysRolePermission::getId).toList();
        if (!removedIds.isEmpty()) {
            removeByIds(removedIds);
        }
        Set<Long> existingPermissionIds = new HashSet<>();
        existing.forEach(binding -> existingPermissionIds.add(binding.getPermissionId()));
        for (Long permissionId : permissionIds) {
            if (!existingPermissionIds.contains(permissionId)) {
                baseMapper.upsertBinding(IdWorker.getId(), roleId, permissionId,
                        tenant.getId(), authenticationContext.getUserId());
            }
        }
        sessionInvalidator.kickoutAfterCommit(userRoleMapper.selectUserIdsByRole(roleId, tenant.getId()));
    }
}

