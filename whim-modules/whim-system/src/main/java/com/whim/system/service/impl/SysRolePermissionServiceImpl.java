package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.mapper.SysRolePermissionMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysRolePermissionDept;
import com.whim.system.model.vo.role.RoleDataScopeVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRolePermissionDeptService;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色功能授权及单个操作的数据范围覆盖。
 */
@Service
@RequiredArgsConstructor
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission>
        implements ISysRolePermissionService {
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final ISysRolePermissionDeptService permissionDeptService;
    private final SysRoleMapper roleMapper;
    private final SysAuthorizationService authorizationService;
    private final AuthenticationSession authenticationSession;
    private final AuthenticationContext authenticationContext;

    /** 读取角色已分配权限ID，不将菜单目录展开为操作权限。 */
    @Override
    public Set<Long> getPermissionIds(Long roleId) {
        authorizationService.requirePermission("system:rolePermission:list");
        roleService.getRequiredRole(roleId);
        return lambdaQuery().eq(SysRolePermission::getRoleId, roleId).list().stream()
                .map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
    }

    /** 全量校验后替换关联，未变化的授权保留操作范围覆盖。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePermissions(Long roleId, Set<Long> permissionIds) {
        authorizationService.requirePermission("system:rolePermission:assign");
        SysRole role = roleService.getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
        if (role.getStatus() != 0) {
            throw new IllegalArgumentException("不能修改已停用角色的权限");
        }
        for (Long permissionId : permissionIds) {
            authorizationService.requireGrantablePermission(permissionId);
        }
        List<SysRolePermission> existing = lambdaQuery().eq(SysRolePermission::getRoleId, roleId).list();
        Set<Long> existingIds = existing.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        var scopes = permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getRoleId, roleId);
        var bindings = lambdaUpdate().eq(SysRolePermission::getRoleId, roleId);
        if (!permissionIds.isEmpty()) {
            scopes.notIn(SysRolePermissionDept::getPermissionId, permissionIds);
            bindings.notIn(SysRolePermission::getPermissionId, permissionIds);
        }
        scopes.remove();
        bindings.remove();
        List<SysRolePermission> additions = permissionIds.stream().filter(id -> !existingIds.contains(id))
                .map(permissionId -> {
                    SysRolePermission binding = new SysRolePermission();
                    binding.setRoleId(roleId);
                    binding.setPermissionId(permissionId);
                    return binding;
                }).toList();
        if (!additions.isEmpty()) {
            saveBatch(additions);
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, roleMapper.selectUserIdsByRole(roleId));
    }

    /** dataScope为空表示继承默认范围，覆盖部门列表仅返回本操作配置。 */
    @Override
    public RoleDataScopeVO getPermissionDataScope(Long roleId, Long permissionId) {
        authorizationService.requirePermission("system:role:dataScope:list");
        roleService.getRequiredRole(roleId);
        SysRolePermission binding = requiredBinding(roleId, permissionId);
        RoleDataScopeVO response = new RoleDataScopeVO();
        response.setRoleId(roleId);
        response.setPermissionId(permissionId);
        response.setDataScope(binding.getDataScope());
        response.setDeptIds(Integer.valueOf(2).equals(binding.getDataScope())
                ? permissionDeptService.lambdaQuery().eq(SysRolePermissionDept::getRoleId, roleId)
                .eq(SysRolePermissionDept::getPermissionId, permissionId).list().stream()
                .map(SysRolePermissionDept::getDeptId).collect(Collectors.toSet()) : Set.of());
        return response;
    }

    /** 覆盖操作级范围或恢复继承，只允许受数据权限保护的操作。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePermissionDataScope(Long roleId, Long permissionId, Integer dataScope, Set<Long> deptIds) {
        authorizationService.requirePermission("system:role:dataScope:assign");
        SysRole role = roleService.getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
        authorizationService.requireGrantablePermission(permissionId);
        SysPermission permission = permissionService.getById(permissionId);
        if (permission.getDataPermission() != 1) {
            throw new IllegalArgumentException("该操作未启用数据权限范围");
        }
        SysRolePermission binding = requiredBinding(roleId, permissionId);
        if (dataScope == null) {
            if (!deptIds.isEmpty()) {
                throw new IllegalArgumentException("恢复继承时不能指定自定义部门");
            }
        } else {
            roleService.validateDataScope(dataScope, deptIds);
        }
        permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getRoleId, roleId)
                .eq(SysRolePermissionDept::getPermissionId, permissionId).remove();
        if (!deptIds.isEmpty()) {
            permissionDeptService.saveBatch(deptIds.stream().map(departmentId -> {
                SysRolePermissionDept scope = new SysRolePermissionDept();
                scope.setRoleId(roleId);
                scope.setPermissionId(permissionId);
                scope.setDeptId(departmentId);
                return scope;
            }).toList());
        }
        lambdaUpdate().eq(SysRolePermission::getId, binding.getId())
                .set(SysRolePermission::getDataScope, dataScope)
                .set(SysRolePermission::getUpdateBy, authenticationContext.getUserId())
                .set(SysRolePermission::getUpdateTime, java.time.LocalDateTime.now()).update();
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, roleMapper.selectUserIdsByRole(roleId));
    }

    /** 校验操作确实分配给该角色，避免配置没有功能授权的数据范围。 */
    private SysRolePermission requiredBinding(Long roleId, Long permissionId) {
        SysRolePermission binding = lambdaQuery().eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getPermissionId, permissionId).one();
        if (binding == null) {
            throw new IllegalArgumentException("该角色尚未获得目标操作权限");
        }
        return binding;
    }
}

