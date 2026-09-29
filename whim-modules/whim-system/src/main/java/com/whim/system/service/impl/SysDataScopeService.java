package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 按本次功能权限计算租户角色的数据范围。
 */
@Service
@RequiredArgsConstructor
public class SysDataScopeService {
    private final ISysTenantService tenantService;
    private final AuthenticationContext authenticationContext;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysDeptMapper deptMapper;
    private final SysDeptPathService deptPathService;

    /** 只合并实际授予本次权限的有效租户角色范围。 */
    public DataScopeDecisionDTO resolveCurrentTenantDataScope(String permissionCode) {
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        Long userId = authenticationContext.getUserId();
        List<RoleInfo> roles = roleMapper.selectAuthorizedDataScopeRoles(userId, tenantId, permissionCode);
        if (roles.isEmpty()) {
            throw new TenantAccessDeniedException("本次操作没有可用的数据范围授权");
        }
        Long memberDeptId = userTenantMapper.selectActiveDepartmentId(userId, tenantId);
        if (memberDeptId != null) {
            try {
                deptPathService.getRequiredActiveDepartment(memberDeptId, tenantId);
            } catch (TenantAccessDeniedException ignored) {
                memberDeptId = null;
            }
        }
        DataScopeDecisionDTO decision = new DataScopeDecisionDTO();
        decision.setTenantId(tenantId);
        decision.setUserId(userId);
        Set<Long> descendantIds = null;
        for (RoleInfo role : roles) {
            switch (role.getDataScope()) {
                case 1 -> {
                    decision.setAll(true);
                    return decision;
                }
                case 2 -> {
                    for (Long deptId : roleDeptMapper.selectActiveDepartmentIds(role.getRoleId(), tenantId)) {
                        try {
                            deptPathService.getRequiredActiveDepartment(deptId, tenantId);
                            decision.getDeptIds().add(deptId);
                        } catch (TenantAccessDeniedException ignored) {
                            // 历史无效部门绑定只缩小范围。
                        }
                    }
                }
                case 3 -> {
                    if (memberDeptId != null) {
                        decision.getDeptIds().add(memberDeptId);
                    }
                }
                case 4 -> {
                    if (memberDeptId != null) {
                        if (descendantIds == null) {
                            descendantIds = deptMapper.selectActiveDescendantIds(memberDeptId, tenantId);
                        }
                        decision.getDeptIds().addAll(descendantIds);
                    }
                }
                case 5 -> decision.setSelf(true);
                case 6 -> {
                    decision.setSelf(true);
                    if (memberDeptId != null) {
                        if (descendantIds == null) {
                            descendantIds = deptMapper.selectActiveDescendantIds(memberDeptId, tenantId);
                        }
                        decision.getDeptIds().addAll(descendantIds);
                    }
                }
                default -> throw new TenantAccessDeniedException("角色数据范围配置无效");
            }
        }
        if (!decision.isAll() && !decision.isSelf() && decision.getDeptIds().isEmpty()) {
            throw new TenantAccessDeniedException("本次操作的数据范围为空");
        }
        return decision;
    }
}
