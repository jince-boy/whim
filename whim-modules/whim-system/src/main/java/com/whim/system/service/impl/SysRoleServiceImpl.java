package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final ISysTenantService tenantService;
    private final SysUserRoleMapper userRoleMapper;
    private final AuthenticationSession authenticationSession;

    /**
     * 查询用户已启用角色的完整信息。
     *
     * @param userId 用户ID
     * @return 角色信息列表
     */
    @Override
    public List<RoleInfo> getRoleInfoListByUserIdAndTenantId(Long userId, Long tenantId) {
        if (Objects.isNull(userId)) {
            return List.of();
        }
        return baseMapper.selectRoleInfoListByUserIdAndTenantId(userId, tenantId);
    }

    /**
     * 判断用户是否拥有全局超级管理员角色。
     *
     * @param userId 用户ID
     * @return true 表示拥有超级管理员角色
     */
    @Override
    public boolean isSuperAdministrator(Long userId) {
        return Boolean.TRUE.equals(baseMapper.selectSuperAdministratorFlag(userId));
    }

    /** 查询当前租户的角色。 */
    @Override
    public List<RoleVO> listCurrentTenantRoles() {
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        return lambdaQuery().eq(SysRole::getTenantId, tenantId)
                .orderByAsc(SysRole::getSort, SysRole::getId).list().stream()
                .map(role -> {
                    RoleVO response = new RoleVO();
                    response.setId(role.getId());
                    response.setRoleName(role.getRoleName());
                    response.setRoleCode(role.getRoleCode());
                    response.setDataScope(role.getDataScope());
                    response.setStatus(role.getStatus());
                    return response;
                }).toList();
    }

    /** 查询角色并确认其属于指定租户。 */
    @Override
    public SysRole getRequiredTenantRole(Long roleId, Long tenantId) {
        SysRole role = getById(roleId);
        if (role == null || !Objects.equals(role.getTenantId(), tenantId) || role.getRoleType() != 1) {
            throw new TenantAccessDeniedException("目标角色不属于当前租户");
        }
        return role;
    }

    /** 创建当前租户角色。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCurrentTenantRole(RoleSaveDTO request) {
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        validateRoleCode(request.getRoleCode(), tenantId, null);
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleType(1);
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(request.getRoleCode().trim());
        role.setDataScope(5);
        role.setSort(0);
        role.setStatus(0);
        save(role);
        return role.getId();
    }

    /** 修改当前租户角色。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentTenantRole(Long roleId, RoleSaveDTO request) {
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        SysRole role = getRequiredTenantRole(roleId, tenantId);
        validateRoleCode(request.getRoleCode(), tenantId, roleId);
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(request.getRoleCode().trim());
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, getUserIdsByRole(roleId, tenantId));
    }

    /** 修改当前租户角色状态。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentTenantRoleStatus(Long roleId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        Long tenantId = tenantService.getRequiredCurrentTenant().getId();
        SysRole role = getRequiredTenantRole(roleId, tenantId);
        role.setStatus(status);
        updateById(role);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, getUserIdsByRole(roleId, tenantId));
    }

    /** 检查当前租户的角色编码唯一且不冒用平台超级管理员。 */
    private void validateRoleCode(String roleCode, Long tenantId, Long excludedRoleId) {
        String normalizedCode = roleCode.trim();
        if (normalizedCode.equals("superadmin") || normalizedCode.equals("*")) {
            throw new IllegalArgumentException("租户角色不能使用平台超级管理员编码");
        }
        List<SysRole> sameCodeRoles = lambdaQuery().eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getRoleCode, normalizedCode).list();
        if (sameCodeRoles.stream().anyMatch(role -> !Objects.equals(role.getId(), excludedRoleId))) {
            throw new IllegalArgumentException("当前租户已存在相同角色编码");
        }
    }

    /** 查询绑定指定租户角色的用户ID。 */
    private List<Long> getUserIdsByRole(Long roleId, Long tenantId) {
        return userRoleMapper.selectObjs(Wrappers.<SysUserRole>lambdaQuery()
                        .select(SysUserRole::getUserId)
                        .eq(SysUserRole::getRoleId, roleId)
                        .eq(SysUserRole::getTenantId, tenantId))
                .stream().map(Long.class::cast).toList();
    }
}

