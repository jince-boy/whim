package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionProvider;
import com.whim.core.permission.DataPermissionScope;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.mapper.SysRolePermissionDeptMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 按精确操作合并有效角色范围，支持操作覆盖范围与部门树。
 */
@Service
@RequiredArgsConstructor
public class SysDataScopeService implements DataPermissionProvider {
    private final AuthenticationContext authenticationContext;
    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRolePermissionDeptMapper permissionDeptMapper;
    private final SysDeptMapper deptMapper;
    private final SysDeptPathService deptPathService;

    /** 查询当前有效身份和授权，拒绝不存在、停用或未授予的操作。 */
    @Override
    public DataPermissionScope resolve(String permissionCode) {
        if (!authenticationContext.isLogin()) {
            throw new DataAccessDeniedException("当前操作需要有效的登录身份");
        }
        SysUser user = userMapper.selectAuthorizationUser(authenticationContext.getUserId());
        if (user == null) {
            throw new DataAccessDeniedException("当前账号不存在或已停用");
        }
        SysPermission permission = permissionMapper.selectOne(Wrappers.<SysPermission>lambdaQuery()
                .eq(SysPermission::getPerms, permissionCode).eq(SysPermission::getStatus, 0));
        if (permission == null || permission.getPerms().isBlank()
                || (permission.getMenuType() != 2 && permission.getMenuType() != 3)) {
            throw new DataAccessDeniedException("操作权限不存在或已停用");
        }
        if (Boolean.TRUE.equals(roleMapper.selectSuperAdministratorFlag(user.getId()))) {
            DataPermissionScope scope = new DataPermissionScope();
            scope.setUserId(user.getId());
            scope.setUserDepartmentId(user.getDeptId());
            scope.setDataProtected(Integer.valueOf(1).equals(permission.getDataPermission()));
            scope.setAll(true);
            return scope;
        }
        List<DataScopeGrantDTO> grants = roleMapper.selectAuthorizedDataScopeRoles(user.getId(), permissionCode);
        if (grants.isEmpty()) {
            throw new DataAccessDeniedException("当前用户未获得本次操作的权限");
        }
        DataPermissionScope scope = resolveGrants(user, grants);
        scope.setDataProtected(Integer.valueOf(1).equals(permission.getDataPermission()));
        return scope;
    }

    /** 按目标账号计算候选角色授予的范围，用于分配角色时验证授权边界。 */
    public DataPermissionScope resolveGrants(SysUser user, List<DataScopeGrantDTO> grants) {
        DataPermissionScope scope = new DataPermissionScope();
        scope.setUserId(user.getId());
        Long departmentId = user.getDeptId();
        if (departmentId != null) {
            try {
                deptPathService.getRequiredActiveDepartment(departmentId);
            } catch (DataAccessDeniedException exception) {
                departmentId = null;
            }
        }
        scope.setUserDepartmentId(departmentId);
        Set<Long> descendants = null;
        for (DataScopeGrantDTO grant : grants) {
            if (!grant.isDataPermission()) {
                scope.setAll(true);
                continue;
            }
            if (grant.getDataScope() == null) {
                throw new DataAccessDeniedException("角色数据范围配置缺失");
            }
            switch (grant.getDataScope()) {
                case 1 -> scope.setAll(true);
                case 2 -> {
                    Set<Long> configured = Boolean.TRUE.equals(grant.getOverridden())
                            ? permissionDeptMapper.selectDepartmentIds(grant.getRoleId(), grant.getPermissionId())
                            : roleDeptMapper.selectDepartmentIds(grant.getRoleId());
                    for (Long configuredId : configured) {
                        try {
                            deptPathService.getRequiredActiveDepartment(configuredId);
                            scope.getDepartmentIds().add(configuredId);
                        } catch (DataAccessDeniedException ignored) {
                            // 无效部门仅缩小范围，不得回退为全部数据。
                        }
                    }
                }
                case 3 -> {
                    if (departmentId != null) {
                        scope.getDepartmentIds().add(departmentId);
                    }
                }
                case 4 -> {
                    if (departmentId != null) {
                        if (descendants == null) {
                            descendants = deptMapper.selectActiveDescendantIds(departmentId);
                        }
                        scope.getDepartmentIds().addAll(descendants);
                    }
                }
                case 5 -> scope.setSelf(true);
                default -> throw new DataAccessDeniedException("角色数据范围配置无效");
            }
        }
        return scope;
    }
}
