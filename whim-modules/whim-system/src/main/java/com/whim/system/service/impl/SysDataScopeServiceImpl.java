package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.DataAccessDeniedException;
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
import com.whim.system.model.enums.SysDataScopeType;
import com.whim.system.service.ISysDataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/16
 * @description 按精确操作合并有效角色范围，批量解析自定义部门和部门层级。
 */
@Service
@RequiredArgsConstructor
public class SysDataScopeServiceImpl implements ISysDataScopeService {
    private final AuthenticationContext authenticationContext;
    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRolePermissionDeptMapper permissionDeptMapper;
    private final SysDeptMapper deptMapper;

    /** 每次读取当前有效授权，角色或部门变化不依赖旧会话中的范围快照。 */
    @Override
    public DataPermissionScope resolve(String permissionCode) {
        if (!authenticationContext.isLogin()) {
            throw new DataAccessDeniedException("当前操作需要有效的登录身份");
        }
        SysUser user = userMapper.selectAuthorizationUser(authenticationContext.getUserId());
        if (user == null) {
            throw new DataAccessDeniedException("当前账号不存在或已停用");
        }
        SysPermission permission = permissionMapper.selectAuthorizationPermission(permissionCode);
        if (permission == null || permission.getPerms().isBlank()) {
            throw new DataAccessDeniedException("操作权限不存在或已停用");
        }
        DataPermissionScope scope;
        if (authenticationContext.isSuperAdministrator()) {
            scope = new DataPermissionScope();
            scope.setUserId(user.getId());
            scope.setUserDepartmentId(user.getDeptId());
            scope.setAll(true);
        } else {
            List<DataScopeGrantDTO> grants = roleMapper.selectAuthorizedDataScopeRoles(user.getId(), permissionCode);
            if (grants.isEmpty()) {
                throw new DataAccessDeniedException("当前用户未获得本次操作的权限");
            }
            scope = resolveGrants(user, grants);
        }
        scope.setDataProtected(Integer.valueOf(1).equals(permission.getDataPermission()));
        return scope;
    }

    /** 合并同一操作的范围；自定义部门批量加载，失效部门及其后代不会扩大授权。 */
    @Override
    public DataPermissionScope resolveGrants(SysUser user, List<DataScopeGrantDTO> grants) {
        DataPermissionScope scope = new DataPermissionScope();
        scope.setUserId(user.getId());
        Set<Long> defaultRoleIds = new HashSet<>();
        List<DataScopeGrantDTO> overriddenGrants = new ArrayList<>();
        boolean includeOwnDepartment = false;
        boolean includeDescendants = false;
        for (DataScopeGrantDTO grant : grants) {
            if (!grant.isDataPermission()) {
                scope.setAll(true);
                continue;
            }
            switch (SysDataScopeType.fromCode(grant.getDataScope())) {
                case ALL -> scope.setAll(true);
                case CUSTOM_DEPARTMENTS -> {
                    if (Boolean.TRUE.equals(grant.getOverridden())) {
                        overriddenGrants.add(grant);
                    } else {
                        defaultRoleIds.add(grant.getRoleId());
                    }
                }
                case OWN_DEPARTMENT -> includeOwnDepartment = true;
                case DEPARTMENT_AND_DESCENDANTS -> includeDescendants = true;
                case SELF -> scope.setSelf(true);
            }
        }

        Set<Long> customDepartmentIds = new HashSet<>();
        if (!scope.isAll()) {
            if (!defaultRoleIds.isEmpty()) {
                customDepartmentIds.addAll(roleDeptMapper.selectDepartmentIdsByRoleIds(defaultRoleIds));
            }
            if (!overriddenGrants.isEmpty()) {
                customDepartmentIds.addAll(permissionDeptMapper.selectDepartmentIdsByGrants(overriddenGrants));
            }
        }
        Set<Long> departmentIds = new HashSet<>(customDepartmentIds);
        if (user.getDeptId() != null) {
            departmentIds.add(user.getDeptId());
        }
        Set<Long> validDepartmentIds = departmentIds.isEmpty()
                ? Set.of() : deptMapper.selectValidDepartmentIds(departmentIds);
        Long departmentId = user.getDeptId() != null && validDepartmentIds.contains(user.getDeptId())
                ? user.getDeptId() : null;
        scope.setUserDepartmentId(departmentId);
        if (scope.isAll()) {
            return scope;
        }
        customDepartmentIds.retainAll(validDepartmentIds);
        scope.getDepartmentIds().addAll(customDepartmentIds);
        if (departmentId != null) {
            if (includeDescendants) {
                scope.getDepartmentIds().addAll(deptMapper.selectActiveDescendantIds(departmentId));
            } else if (includeOwnDepartment) {
                scope.getDepartmentIds().add(departmentId);
            }
        }
        return scope;
    }
}
