package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ISysUserTenantService;
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
 * @description 系统用户角色关联表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
    private final ISysUserTenantService userTenantService;
    private final ISysRoleService roleService;
    private final ISysUserService userService;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;

    /** 按角色查询动作的数据范围读取目标成员的角色 ID。 */
    @Override
    public Set<Long> getCurrentTenantRoleIds(Long userId) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:userRole:list");
        Long tenantId = scope.getTenantId();
        userTenantService.getRequiredMemberInDataScope(userId, scope);
        Set<Long> roleIds = new LinkedHashSet<>();
        for (SysUserRole binding : lambdaQuery().eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId).list()) {
            roleIds.add(binding.getRoleId());
        }
        return roleIds;
    }

    /** 按角色分配动作的数据范围覆盖目标成员角色并撤销旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCurrentTenantRoles(Long userId, Set<Long> roleIds) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:userRole:assign");
        Long tenantId = scope.getTenantId();
        SysUserTenant member = userTenantService.lockRequiredMemberInDataScope(userId, scope);
        SysUser user = userService.getById(userId);
        if (member.getStatus() != 0 || user == null || user.getStatus() != 0) {
            throw new TenantAccessDeniedException("目标成员不可用");
        }
        for (Long roleId : roleIds) {
            SysRole role = roleService.getRequiredTenantRole(roleId, tenantId);
            if (role.getStatus() != 0) {
                throw new IllegalArgumentException("不能分配已停用的角色");
            }
        }
        List<SysUserRole> existing = lambdaQuery().eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId).list();
        List<Long> removedIds = existing.stream().filter(binding -> !roleIds.contains(binding.getRoleId()))
                .map(SysUserRole::getId).toList();
        if (!removedIds.isEmpty()) {
            removeByIds(removedIds);
        }
        Set<Long> existingRoleIds = new HashSet<>();
        existing.forEach(binding -> existingRoleIds.add(binding.getRoleId()));
        for (Long roleId : roleIds) {
            if (!existingRoleIds.contains(roleId)) {
                baseMapper.upsertBinding(IdWorker.getId(), userId, roleId, tenantId,
                        authenticationContext.getUserId());
            }
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

}

