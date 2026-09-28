package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.whim.system.service.AuthorizationSessionInvalidator;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.mapper.SysTenantPackagePermissionMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysTenantPackagePermission;
import com.whim.system.model.vo.permission.PermissionVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统权限菜单表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    /**
     * 认证会话操作对象
     */
    private final AuthorizationSessionInvalidator authorizationSessionInvalidator;
    private final ISysTenantService tenantService;
    private final SysTenantPackagePermissionMapper packagePermissionMapper;

    /**
     * 查询用户已启用权限编码集合。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @param roleIds 当前用户已启用角色ID集合
     * @return 权限编码集合
     */
    @Override
    public Set<String> getPermissionCodeSetByUserIdAndTenantId(Long userId, Long tenantId, Set<Long> roleIds) {
        if (Objects.isNull(userId) || roleIds.isEmpty()) {
            return Set.of();
        }
        return Objects.requireNonNullElse(
                baseMapper.selectPermissionCodeSetByUserIdAndTenantId(userId, tenantId, roleIds),
                Set.of()
        );
    }

    /** 查询当前租户套餐内可分配的功能权限。 */
    @Override
    public List<PermissionVO> listCurrentTenantAssignablePermissions() {
        SysTenant tenant = tenantService.getRequiredCurrentTenant();
        Set<Long> permissionIds = new HashSet<>();
        for (SysTenantPackagePermission binding : packagePermissionMapper.selectList(
                Wrappers.<SysTenantPackagePermission>lambdaQuery()
                        .eq(SysTenantPackagePermission::getPackageId, tenant.getPackageId()))) {
            permissionIds.add(binding.getPermissionId());
        }
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery().in(SysPermission::getId, permissionIds)
                .eq(SysPermission::getStatus, 0).orderByAsc(SysPermission::getSort, SysPermission::getId)
                .list().stream()
                .filter(permission -> !permission.getPerms().startsWith("system:platform:"))
                .map(this::toPermissionVO).toList();
    }

    /** 查询全局权限目录。 */
    @Override
    public List<PermissionVO> listPlatformPermissions() {
        tenantService.requirePlatformAdministrator();
        return lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId)
                .list().stream().map(this::toPermissionVO).toList();
    }

    /** 修改权限状态并在提交后撤销相关用户的旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPermissionStatus(Long permissionId, Integer status) {
        tenantService.requirePlatformAdministrator();
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysPermission permission = getById(permissionId);
        if (permission == null) {
            throw new IllegalArgumentException("目标权限不存在");
        }
        permission.setStatus(status);
        updateById(permission);
    }

    /**
     * 修改权限，提交成功后使受影响用户的旧授权失效。
     *
     * @param entity 权限实体
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysPermission entity) {
        Set<Long> affectedUserIds = baseMapper.selectUserIdSetByPermissionId(entity.getId());
        boolean updated = super.updateById(entity);
        if (updated && !affectedUserIds.isEmpty()) {
            authorizationSessionInvalidator.kickoutAfterCommit(affectedUserIds);
        }
        return updated;
    }

    /** 转换对外权限响应。 */
    private PermissionVO toPermissionVO(SysPermission permission) {
        PermissionVO response = new PermissionVO();
        response.setId(permission.getId());
        response.setMenuName(permission.getMenuName());
        response.setPerms(permission.getPerms());
        response.setStatus(permission.getStatus());
        return response;
    }
}

