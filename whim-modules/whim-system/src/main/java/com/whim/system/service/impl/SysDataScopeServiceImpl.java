package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.model.enums.SysDataScopeType;
import com.whim.system.service.ISysDataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/16
 * @description 根据登录上下文合并当前操作的角色范围，按需加载部门。
 */
@Service
@RequiredArgsConstructor
public class SysDataScopeServiceImpl implements ISysDataScopeService {
    private final AuthenticationContext authenticationContext;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 从登录快照读取身份和授权，仅查询自定义部门及部门层级。
     */
    @Override
    public DataPermissionScope resolve(String permissionCode) {
        if (!authenticationContext.isLogin()) {
            throw new DataAccessDeniedException("当前操作需要有效的登录身份");
        }
        UserInfo user = authenticationContext.getCurrentUserInfo();
        if (!user.getPermissionCodeSet().contains(permissionCode)) {
            throw new DataAccessDeniedException("当前用户未获得本次操作的权限");
        }
        DataPermissionScope scope = new DataPermissionScope();
        scope.setUserId(user.getUserId());
        if (authenticationContext.isSuperAdministrator()) {
            scope.setAll(true);
            return scope;
        }
        Set<Long> roleIds = user.getPermissionRoleIds().getOrDefault(permissionCode, Set.of());
        List<RoleInfo> roles = user.getRoleInfoList().stream()
                .filter(role -> roleIds.contains(role.getRoleId())).toList();
        if (roles.isEmpty()) {
            throw new DataAccessDeniedException("登录上下文缺少本次操作的角色授权，请重新登录");
        }
        Set<Long> customRoleIds = new HashSet<>();
        Set<Long> departmentIds = scope.getDepartmentIds();
        boolean includeDescendants = false;
        for (RoleInfo role : roles) {
            switch (SysDataScopeType.fromCode(role.getDataScope())) {
                case ALL -> {
                    scope.setAll(true);
                    return scope;
                }
                case CUSTOM_DEPARTMENTS -> customRoleIds.add(role.getRoleId());
                case OWN_DEPARTMENT -> departmentIds.add(user.getDeptId());
                case DEPARTMENT_AND_DESCENDANTS -> {
                    departmentIds.add(user.getDeptId());
                    includeDescendants = true;
                }
                case SELF -> scope.setSelf(true);
            }
        }

        // 自定义部门统一加载，再校验候选部门及其祖先链。
        if (!customRoleIds.isEmpty()) {
            departmentIds.addAll(roleDeptMapper.selectDepartmentIdsByRoleIds(customRoleIds));
        }
        if (!departmentIds.isEmpty()) {
            departmentIds.retainAll(deptMapper.selectValidDepartmentIds(departmentIds));
        }
        if (includeDescendants && departmentIds.contains(user.getDeptId())) {
            departmentIds.addAll(deptMapper.selectActiveDescendantIds(user.getDeptId()));
        }
        return scope;
    }
}
