package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysRolePermissionDept;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.vo.role.RoleDataScopeVO;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysRoleDeptService;
import com.whim.system.service.ISysRolePermissionDeptService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统角色及默认数据范围管理。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final SysAuthorizationService authorizationService;
    private final ISysRoleDeptService roleDeptService;
    private final ISysRolePermissionDeptService permissionDeptService;
    private final ObjectProvider<ISysRolePermissionService> permissionServices;
    private final ISysDeptService deptService;
    private final AuthenticationSession authenticationSession;

    /** 查询当前有效角色。 */
    @Override
    public List<RoleInfo> getRoleInfoListByUserId(Long userId) {
        return baseMapper.selectRoleInfoListByUserId(userId);
    }

    /** 查询有效超级管理员身份。 */
    @Override
    public boolean isSuperAdministrator(Long userId) {
        return Boolean.TRUE.equals(baseMapper.selectSuperAdministratorFlag(userId));
    }

    /** 查询系统角色目录。 */
    @Override
    public List<RoleVO> listRoles() {
        authorizationService.requirePermission("system:role:list");
        return lambdaQuery().orderByAsc(SysRole::getSort, SysRole::getId).list().stream().map(role -> {
            RoleVO response = new RoleVO();
            response.setId(role.getId());
            response.setRoleName(role.getRoleName());
            response.setRoleCode(role.getRoleCode());
            response.setDataScope(role.getDataScope());
            response.setStatus(role.getStatus());
            return response;
        }).toList();
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

    /** 创建未授予任何功能且默认仅本人范围的角色。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleSaveDTO request) {
        authorizationService.requirePermission("system:role:create");
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(validateRoleCode(request.getRoleCode(), null));
        role.setDataScope(5);
        role.setSort(0);
        role.setStatus(0);
        save(role);
        return role.getId();
    }

    /** 修改普通角色并在提交后撤销旧角色快照。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleSaveDTO request) {
        authorizationService.requirePermission("system:role:update");
        SysRole role = getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(validateRoleCode(request.getRoleCode(), roleId));
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 修改角色状态，内置超级管理员角色不可停用。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRoleStatus(Long roleId, Integer status) {
        authorizationService.requirePermission("system:role:status");
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysRole role = getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
        role.setStatus(status);
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.selectUserIdsByRole(roleId));
    }

    /** 读取角色默认范围及其自定义部门。 */
    @Override
    public RoleDataScopeVO getDataScope(Long roleId) {
        authorizationService.requirePermission("system:role:dataScope:list");
        SysRole role = getRequiredRole(roleId);
        RoleDataScopeVO response = new RoleDataScopeVO();
        response.setRoleId(roleId);
        response.setDataScope(role.getDataScope());
        response.setDeptIds(role.getDataScope() == 2
                ? roleDeptService.lambdaQuery().eq(SysRoleDept::getRoleId, roleId).list().stream()
                .map(SysRoleDept::getDeptId).collect(Collectors.toSet()) : Set.of());
        return response;
    }

    /** 覆盖默认范围并使引用该角色的旧会话失效。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDataScope(Long roleId, Integer dataScope, Set<Long> deptIds) {
        authorizationService.requirePermission("system:role:dataScope:assign");
        SysRole role = getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
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

    /** 删除未被用户持有的普通角色，软删后其操作授权不再生效。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        authorizationService.requirePermission("system:role:delete");
        SysRole role = getRequiredRole(roleId);
        authorizationService.requireMutableRole(role);
        if (!baseMapper.selectUserIdsByRole(roleId).isEmpty()) {
            throw new IllegalArgumentException("角色仍关联用户，不能删除");
        }
        roleDeptService.lambdaUpdate().eq(SysRoleDept::getRoleId, roleId).remove();
        permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getRoleId, roleId).remove();
        permissionServices.getObject().lambdaUpdate().eq(SysRolePermission::getRoleId, roleId).remove();
        role.setDeleted(1);
        removeById(role);
    }

    /** 默认与操作级配置共用五种范围参数验证。 */
    @Override
    public void validateDataScope(Integer dataScope, Set<Long> deptIds) {
        if (dataScope == null || dataScope < 1 || dataScope > 5) {
            throw new IllegalArgumentException("数据范围只能为1至5");
        }
        if (dataScope == 2 && deptIds.isEmpty()) {
            throw new IllegalArgumentException("自定义范围至少需要一个部门");
        }
        if (dataScope != 2 && !deptIds.isEmpty()) {
            throw new IllegalArgumentException("非自定义范围不能指定部门");
        }
        for (Long departmentId : deptIds) {
            deptService.getRequiredActiveDepartment(departmentId);
        }
    }

    /** 标准化角色编码并保留内置管理员编码，活动角色编码全局唯一。 */
    private String validateRoleCode(String roleCode, Long excludedRoleId) {
        String code = roleCode.trim().toLowerCase(Locale.ROOT);
        if (!code.matches("[a-z][a-z0-9:]*") || "superadmin".equals(code)) {
            throw new IllegalArgumentException("角色编码须为字母、数字或冒号，且不能使用内置管理员编码");
        }
        if (lambdaQuery().eq(SysRole::getRoleCode, code).list().stream()
                .anyMatch(role -> !Objects.equals(role.getId(), excludedRoleId))) {
            throw new IllegalArgumentException("已存在相同角色编码");
        }
        return code;
    }
}

