package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色委派边界与管理员保护，独立于菜单展示和行级过滤。
 */
@Service
@RequiredArgsConstructor
public class SysAuthorizationService {
    private final AuthenticationContext authenticationContext;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysDataScopeService dataScopeService;

    /** 每次业务操作确认账号及精确权限仍有效。 */
    public void requirePermission(String permissionCode) {
        dataScopeService.resolve(permissionCode);
    }

    /** 判断当前账号是否仍持有有效超级管理员角色。 */
    public boolean isSuperAdministrator() {
        return Boolean.TRUE.equals(roleMapper.selectSuperAdministratorFlag(currentUser().getId()));
    }

    /** 禁止修改内置超级管理员角色，普通管理员不能修改本人持有角色。 */
    public void requireMutableRole(SysRole role) {
        if ("superadmin".equals(role.getRoleCode())) {
            throw new DataAccessDeniedException("内置超级管理员角色不能修改或删除");
        }
        Long userId = currentUser().getId();
        if (!isSuperAdministrator() && roleMapper.selectAdministrativeRolesByUserId(userId).stream()
                .anyMatch(assigned -> Objects.equals(assigned.getId(), role.getId()))) {
            throw new DataAccessDeniedException("不能修改本人持有角色的授权配置");
        }
        if (!isSuperAdministrator()) {
            for (DataScopeGrantDTO grant : roleMapper.selectRoleGrants(role.getId())) {
                requireGrantablePermission(grant.getPermissionId());
            }
        }
    }

    /** 普通管理员只能配置自己拥有完整数据范围的操作，不能通过角色编辑扩大权限。 */
    public void requireGrantablePermission(Long permissionId) {
        SysPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getStatus() != 0) {
            throw new IllegalArgumentException("目标权限不存在或已停用");
        }
        if (permission.getMenuType() == 1 || permission.getMenuType() == 4) {
            return;
        }
        DataPermissionScope available = dataScopeService.resolve(permission.getPerms());
        if (!available.isAll()) {
            throw new DataAccessDeniedException("不能授予超出本人完整授权范围的操作");
        }
    }

    /** 按目标账号的主部门计算候选角色，确保其权限和数据范围不超出操作者。 */
    public void requireDelegableRole(SysRole role, SysUser targetUser) {
        if (role.getStatus() != 0) {
            throw new IllegalArgumentException("不能分配已停用角色");
        }
        if (isSuperAdministrator()) {
            return;
        }
        if ("superadmin".equals(role.getRoleCode())) {
            throw new DataAccessDeniedException("只有超级管理员可以授予超级管理员角色");
        }
        for (DataScopeGrantDTO grant : roleMapper.selectRoleGrants(role.getId())) {
            SysPermission permission = permissionMapper.selectById(grant.getPermissionId());
            if (permission.getMenuType() == 1 || permission.getMenuType() == 4) {
                continue;
            }
            DataPermissionScope available = dataScopeService.resolve(permission.getPerms());
            DataPermissionScope requested = dataScopeService.resolveGrants(targetUser, List.of(grant));
            if (available.isAll()) {
                continue;
            }
            if (requested.isAll() || !available.getDepartmentIds().containsAll(requested.getDepartmentIds())
                    || (requested.isSelf() && !(available.isSelf()
                    && Objects.equals(available.getUserId(), targetUser.getId())))) {
                throw new DataAccessDeniedException("候选角色授权超出本人可委派范围");
            }
        }
    }

    /** 普通管理员不能管理超级管理员账号。 */
    public void requireManageableUser(Long userId) {
        if (Boolean.TRUE.equals(roleMapper.selectHasSuperAdministratorRole(userId)) && !isSuperAdministrator()) {
            throw new DataAccessDeniedException("只有超级管理员可以管理超级管理员账号");
        }
    }

    /** 恢复账号或迁移部门前重新核验其全部候选角色的授权边界。 */
    public void requireDelegableUserRoles(SysUser targetUser) {
        if (isSuperAdministrator()) {
            return;
        }
        for (SysRole role : roleMapper.selectAdministrativeRolesByUserId(targetUser.getId())) {
            if (role.getStatus() == 0) {
                requireDelegableRole(role, targetUser);
            }
        }
    }

    /** 在同一事务持有内置角色行锁，保护最后一个有效管理员。 */
    public void lockAdministratorMembership() {
        if (roleMapper.lockSuperAdministratorRole() == null) {
            throw new DataAccessDeniedException("系统缺少内置超级管理员角色");
        }
    }

    /** 检查停用、删除或移除角色不会消灭最后一个有效超级管理员。 */
    public void requireAdministratorCanBeRemoved(Long userId) {
        if (Boolean.TRUE.equals(roleMapper.selectSuperAdministratorFlag(userId))
                && roleMapper.countActiveSuperAdministrators() <= 1) {
            throw new DataAccessDeniedException("不能移除最后一个有效超级管理员");
        }
    }

    /** 查询授权元数据身份，不使用客户端或旧会话的账号状态。 */
    private SysUser currentUser() {
        if (!authenticationContext.isLogin()) {
            throw new DataAccessDeniedException("当前操作需要登录");
        }
        SysUser user = userMapper.selectAuthorizationUser(authenticationContext.getUserId());
        if (user == null) {
            throw new DataAccessDeniedException("当前账号不存在或已停用");
        }
        return user;
    }
}
