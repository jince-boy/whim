package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysRolePermissionDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysDataScopeType;
import com.whim.system.model.enums.SysMenuType;
import com.whim.system.model.vo.role.RoleDataScopeVO;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.service.ISysDataScopeService;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleDeptService;
import com.whim.system.service.ISysRolePermissionDeptService;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统角色、默认数据范围、授权委派及管理员角色保护。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final AuthenticationContext authenticationContext;
    private final ISysPermissionService permissionService;
    private final ISysDataScopeService dataScopeService;
    private final ISysRoleDeptService roleDeptService;
    private final ISysRolePermissionDeptService permissionDeptService;
    private final ISysRolePermissionService rolePermissionService;
    private final ISysDeptService deptService;
    private final AuthenticationSession authenticationSession;

    /** 读取角色默认范围及其自定义部门。 */
    @Override
    public RoleDataScopeVO getDataScope(Long roleId) {
        SysRole role = getRequiredRole(roleId);
        RoleDataScopeVO response = new RoleDataScopeVO();
        response.setRoleId(roleId);
        response.setDataScope(role.getDataScope());
        response.setDeptIds(role.getDataScope() == SysDataScopeType.CUSTOM_DEPARTMENTS.getCode()
                ? roleDeptService.lambdaQuery().eq(SysRoleDept::getRoleId, roleId).list().stream()
                .map(SysRoleDept::getDeptId).collect(Collectors.toSet()) : Set.of());
        return response;
    }

    /** 读取角色已分配权限ID，不将菜单目录展开为操作权限。 */
    @Override
    public Set<Long> getPermissionIds(Long roleId) {
        getRequiredRole(roleId);
        return rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId).list().stream()
                .map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
    }

    /** dataScope为空表示继承默认范围，覆盖部门列表仅返回本操作配置。 */
    @Override
    public RoleDataScopeVO getPermissionDataScope(Long roleId, Long permissionId) {
        getRequiredRole(roleId);
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

    /** 查询当前有效角色。 */
    @Override
    public List<RoleInfo> getRoleInfoListByUserId(Long userId) {
        return baseMapper.selectRoleInfoListByUserId(userId);
    }

    /** 查询系统角色目录。 */
    @Override
    public List<RoleVO> listRoles() {
        return BeanConvertUtils.convertList(
                lambdaQuery().orderByAsc(SysRole::getSort, SysRole::getId).list(), RoleVO.class);
    }

    /** 获取角色并锁定；写操作在服务事务内调用。 */
    @Override
    public SysRole getRequiredRole(Long roleId) {
        SysRole role = lambdaQuery().eq(SysRole::getId, roleId).last("FOR UPDATE").one();
        if (role == null) {
            throw new IllegalArgumentException("目标角色不存在");
        }
        return role;
    }

    /** 识别目标账号的管理员角色，不因账号停用而遗漏保护。 */
    @Override
    public boolean hasSuperAdministratorRole(Long userId) {
        return Boolean.TRUE.equals(baseMapper.selectHasSuperAdministratorRole(userId));
    }

    /** 创建未授予任何功能且默认仅本人范围的角色。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleSaveDTO request) {
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(validateRoleCode(request.getRoleCode(), null));
        role.setDataScope(SysDataScopeType.SELF.getCode());
        role.setSort(0);
        role.setStatus(0);
        save(role);
        return role.getId();
    }

    /** 修改普通角色并在提交后撤销旧角色快照。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleSaveDTO request) {
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(validateRoleCode(request.getRoleCode(), roleId));
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 修改角色状态，内置超级管理员角色不可停用。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRoleStatus(Long roleId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        role.setStatus(status);
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 覆盖默认范围并使引用该角色的旧会话失效。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDataScope(Long roleId, Integer dataScope, Set<Long> deptIds) {
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        validateDataScope(dataScope, deptIds);
        roleDeptService.lambdaUpdate().eq(SysRoleDept::getRoleId, roleId).remove();
        if (!deptIds.isEmpty()) {
            roleDeptService.saveBatch(deptIds.stream().map(departmentId -> {
                SysRoleDept binding = new SysRoleDept();
                binding.setRoleId(roleId);
                binding.setDeptId(departmentId);
                return binding;
            }).toList());
        }
        role.setDataScope(dataScope);
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 全量校验后替换关联，未变化的授权保留操作范围覆盖。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePermissions(Long roleId, Set<Long> permissionIds) {
        // 与目录删除共享锁，不能把角色授权提交到已删除的权限节点。
        permissionService.lockCatalog();
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        if (role.getStatus() != 0) {
            throw new IllegalArgumentException("不能修改已停用角色的权限");
        }
        for (Long permissionId : permissionIds) {
            permissionService.requireGrantablePermission(permissionId);
        }
        List<SysRolePermission> existing = rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId).list();
        Set<Long> existingIds = existing.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        var scopes = permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getRoleId, roleId);
        var bindings = rolePermissionService.lambdaUpdate().eq(SysRolePermission::getRoleId, roleId);
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
            rolePermissionService.saveBatch(additions);
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 覆盖操作级范围或恢复继承，只允许受数据权限保护的操作。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePermissionDataScope(Long roleId, Long permissionId, Integer dataScope, Set<Long> deptIds) {
        // 与菜单删除串行执行，避免为已删除的权限写入操作范围。
        permissionService.lockCatalog();
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        SysPermission permission = permissionService.requireGrantablePermission(permissionId);
        if (permission.getDataPermission() != 1) {
            throw new IllegalArgumentException("该操作未启用数据权限范围");
        }
        SysRolePermission binding = requiredBinding(roleId, permissionId);
        if (dataScope == null) {
            if (!deptIds.isEmpty()) {
                throw new IllegalArgumentException("恢复继承时不能指定自定义部门");
            }
        } else {
            validateDataScope(dataScope, deptIds);
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
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getId, binding.getId())
                .set(SysRolePermission::getDataScope, dataScope).update(new SysRolePermission());
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 删除未被用户持有的普通角色，软删后其操作授权不再生效。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = getRequiredRole(roleId);
        requireMutableRole(role);
        if (!baseMapper.selectUserIdsByRole(roleId).isEmpty()) {
            throw new IllegalArgumentException("角色仍关联用户，不能删除");
        }
        roleDeptService.lambdaUpdate().eq(SysRoleDept::getRoleId, roleId).remove();
        permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getRoleId, roleId).remove();
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getRoleId, roleId).remove();
        role.setDeleted(1);
        removeById(role);
    }

    /** 禁止修改内置超级管理员角色，普通管理员不能修改本人持有角色。 */
    @Override
    public void requireMutableRole(SysRole role) {
        if ("superadmin".equals(role.getRoleCode())) {
            throw new DataAccessDeniedException("内置超级管理员角色不能修改或删除");
        }
        Long userId = authenticationContext.getUserId();
        boolean administrator = authenticationContext.isSuperAdministrator();
        if (!administrator && baseMapper.selectAdministrativeRolesByUserId(userId).stream()
                .anyMatch(assigned -> Objects.equals(assigned.getId(), role.getId()))) {
            throw new DataAccessDeniedException("不能修改本人持有角色的授权配置");
        }
        if (!administrator) {
            for (DataScopeGrantDTO grant : baseMapper.selectRoleGrants(role.getId())) {
                permissionService.requireGrantablePermission(grant.getPermissionId());
            }
        }
    }

    /** 按目标账号的主部门计算候选角色，确保其权限和数据范围不超出操作者。 */
    @Override
    public void requireDelegableRole(SysRole role, SysUser targetUser) {
        if (role.getStatus() != 0) {
            throw new IllegalArgumentException("不能分配已停用角色");
        }
        if (authenticationContext.isSuperAdministrator()) {
            return;
        }
        if ("superadmin".equals(role.getRoleCode())) {
            throw new DataAccessDeniedException("只有超级管理员可以授予超级管理员角色");
        }
        for (DataScopeGrantDTO grant : baseMapper.selectRoleGrants(role.getId())) {
            if (SysMenuType.DIRECTORY.matches(grant.getMenuType())
                    || SysMenuType.EXTERNAL_LINK.matches(grant.getMenuType())) {
                continue;
            }
            DataPermissionScope available = dataScopeService.resolve(grant.getPermissionCode());
            if (available.isAll()) {
                continue;
            }
            DataPermissionScope requested = dataScopeService.resolveGrants(targetUser, List.of(grant));
            if (requested.isAll() || !available.getDepartmentIds().containsAll(requested.getDepartmentIds())
                    || (requested.isSelf() && !(available.isSelf()
                    && Objects.equals(available.getUserId(), targetUser.getId())))) {
                throw new DataAccessDeniedException("候选角色授权超出本人可委派范围");
            }
        }
    }

    /** 恢复账号或迁移部门前重新核验其全部候选角色的授权边界。 */
    @Override
    public void requireDelegableUserRoles(SysUser targetUser) {
        if (authenticationContext.isSuperAdministrator()) {
            return;
        }
        for (SysRole role : baseMapper.selectAdministrativeRolesByUserId(targetUser.getId())) {
            if (role.getStatus() == 0) {
                requireDelegableRole(role, targetUser);
            }
        }
    }

    /** 在同一事务持有内置角色行锁，保护最后一个有效管理员。 */
    @Override
    public void lockAdministratorMembership() {
        if (lambdaQuery().select(SysRole::getId).eq(SysRole::getRoleCode, "superadmin")
                .last("FOR UPDATE").one() == null) {
            throw new DataAccessDeniedException("系统缺少内置超级管理员角色");
        }
    }

    /** 检查停用、删除或移除角色不会消灭最后一个有效超级管理员。 */
    @Override
    public void requireAdministratorCanBeRemoved(Long userId) {
        if (baseMapper.selectSuperAdministratorFlag(userId)
                && baseMapper.countActiveSuperAdministrators() <= 1) {
            throw new DataAccessDeniedException("不能移除最后一个有效超级管理员");
        }
    }

    /** 默认与操作级配置共用五种范围参数验证。 */
    @Override
    public void validateDataScope(Integer dataScope, Set<Long> deptIds) {
        if (dataScope == null || dataScope < 1 || dataScope > 5) {
            throw new IllegalArgumentException("数据范围只能为1至5");
        }
        if (dataScope == SysDataScopeType.CUSTOM_DEPARTMENTS.getCode() && deptIds.isEmpty()) {
            throw new IllegalArgumentException("自定义范围至少需要一个部门");
        }
        if (dataScope != SysDataScopeType.CUSTOM_DEPARTMENTS.getCode() && !deptIds.isEmpty()) {
            throw new IllegalArgumentException("非自定义范围不能指定部门");
        }
        for (Long departmentId : deptIds) {
            deptService.getRequiredActiveDepartment(departmentId);
        }
    }

    /** 校验操作确实分配给该角色，避免配置没有功能授权的数据范围。 */
    private SysRolePermission requiredBinding(Long roleId, Long permissionId) {
        SysRolePermission binding = rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, roleId)
                .eq(SysRolePermission::getPermissionId, permissionId).one();
        if (binding == null) {
            throw new IllegalArgumentException("该角色尚未获得目标操作权限");
        }
        return binding;
    }
    /** 标准化角色编码并保留内置管理员编码，活动角色编码全局唯一。 */
    private String validateRoleCode(String roleCode, Long excludedRoleId) {
        String code = roleCode.trim().toLowerCase(Locale.ROOT);
        if (!code.matches("[a-z][a-z0-9:]*") || "superadmin".equals(code)) {
            throw new IllegalArgumentException("角色编码须为字母、数字或冒号，且不能使用内置管理员编码");
        }
        if (lambdaQuery().eq(SysRole::getRoleCode, code)
                .ne(excludedRoleId != null, SysRole::getId, excludedRoleId).exists()) {
            throw new IllegalArgumentException("已存在相同角色编码");
        }
        return code;
    }
}

