package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户角色分配，独立核验目标用户范围与角色可委派范围。
 */
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
    private final SysUserMapper userMapper;
    private final ISysRoleService roleService;
    private final SysAuthorizationService authorizationService;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;

    /** 只有本次操作可见用户的角色关联可以读取。 */
    @Override
    @DataPermission(permission = "system:userRole:list",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public Set<Long> getRoleIds(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new DataAccessDeniedException("用户不存在或不在本次操作范围内");
        }
        return lambdaQuery().eq(SysUserRole::getUserId, userId).list().stream()
                .map(SysUserRole::getRoleId).collect(Collectors.toSet());
    }

    /** 全部候选和移除角色验证通过后替换关联，并保护最后一个管理员。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:userRole:assign",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void replaceRoles(Long userId, Set<Long> roleIds) {
        authorizationService.lockAdministratorMembership();
        SysUser user = userMapper.selectForUpdate(userId);
        if (user == null) {
            throw new DataAccessDeniedException("用户不存在或不在本次操作范围内");
        }
        authorizationService.requireManageableUser(userId);
        if (Objects.equals(userId, authenticationContext.getUserId())
                && !authorizationService.isSuperAdministrator()) {
            throw new DataAccessDeniedException("不能修改本人的角色关联");
        }
        if (user.getStatus() != 0 && !roleIds.isEmpty()) {
            throw new IllegalArgumentException("不能为停用账号分配角色");
        }
        List<SysUserRole> existing = lambdaQuery().eq(SysUserRole::getUserId, userId).list();
        Set<Long> existingIds = existing.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> involved = new HashSet<>(existingIds);
        involved.addAll(roleIds);
        boolean retainsAdministrator = false;
        for (Long roleId : involved.stream().sorted().toList()) {
            SysRole role = roleService.getRequiredRole(roleId);
            if (roleIds.contains(roleId)) {
                authorizationService.requireDelegableRole(role, user);
                retainsAdministrator |= "superadmin".equals(role.getRoleCode());
            } else if (!authorizationService.isSuperAdministrator() && role.getStatus() == 0) {
                authorizationService.requireDelegableRole(role, user);
            }
        }
        if (!retainsAdministrator) {
            authorizationService.requireAdministratorCanBeRemoved(userId);
        }
        var removals = lambdaUpdate().eq(SysUserRole::getUserId, userId);
        if (!roleIds.isEmpty()) {
            removals.notIn(SysUserRole::getRoleId, roleIds);
        }
        removals.remove();
        List<SysUserRole> additions = roleIds.stream().filter(id -> !existingIds.contains(id)).map(roleId -> {
            SysUserRole binding = new SysUserRole();
            binding.setUserId(userId);
            binding.setRoleId(roleId);
            return binding;
        }).toList();
        if (!additions.isEmpty()) {
            saveBatch(additions);
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }
}

