package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysTenantPackagePermissionMapper;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysTenantPackage;
import com.whim.system.model.entity.SysTenantPackagePermission;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysTenantPackageService;
import com.whim.system.service.ISysTenantPackagePermissionService;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统租户套餐权限关联表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysTenantPackagePermissionServiceImpl extends ServiceImpl<SysTenantPackagePermissionMapper, SysTenantPackagePermission> implements ISysTenantPackagePermissionService {
    private final ISysTenantService tenantService;
    private final ISysTenantPackageService tenantPackageService;
    private final ISysPermissionService permissionService;
    private final SysUserTenantMapper userTenantMapper;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;

    /** 覆盖套餐权限并在提交后撤销受影响成员的旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePackagePermissions(Long packageId, Set<Long> permissionIds) {
        tenantService.requirePlatformAdministrator();
        SysTenantPackage tenantPackage = tenantPackageService.getById(packageId);
        if (tenantPackage == null || tenantPackage.getStatus() != 0) {
            throw new TenantAccessDeniedException("租户套餐不可用");
        }
        if (!permissionIds.isEmpty()) {
            List<SysPermission> permissions = permissionService.listByIds(permissionIds);
            if (permissions.size() != permissionIds.size()
                    || permissions.stream().anyMatch(permission -> permission.getStatus() != 0
                    || permission.getPerms().startsWith("system:platform:"))) {
                throw new IllegalArgumentException("权限不存在、已停用或属于平台范围");
            }
        }
        List<SysTenantPackagePermission> existing = lambdaQuery()
                .eq(SysTenantPackagePermission::getPackageId, packageId).list();
        List<Long> removedIds = existing.stream()
                .filter(binding -> !permissionIds.contains(binding.getPermissionId()))
                .map(SysTenantPackagePermission::getId).toList();
        if (!removedIds.isEmpty()) {
            removeByIds(removedIds);
        }
        Set<Long> existingPermissionIds = new HashSet<>();
        existing.forEach(binding -> existingPermissionIds.add(binding.getPermissionId()));
        for (Long permissionId : permissionIds) {
            if (!existingPermissionIds.contains(permissionId)) {
                baseMapper.upsertBinding(IdWorker.getId(), packageId, permissionId,
                        authenticationContext.getUserId());
            }
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                userTenantMapper.selectMemberUserIdsByPackageId(packageId));
    }
}

