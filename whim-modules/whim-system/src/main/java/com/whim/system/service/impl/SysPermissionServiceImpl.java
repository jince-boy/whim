package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.permission.PermissionVO;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 统一权限目录及操作状态管理。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
    private final SysAuthorizationService authorizationService;
    private final SysRoleMapper roleMapper;
    private final AuthenticationSession authenticationSession;

    /** 查询账号通过有效角色获得的精确权限码。 */
    @Override
    public Set<String> getPermissionCodeSetByUserId(Long userId) {
        if (Boolean.TRUE.equals(roleMapper.selectSuperAdministratorFlag(userId))) {
            return lambdaQuery().eq(SysPermission::getStatus, 0).in(SysPermission::getMenuType, 2, 3)
                    .isNotNull(SysPermission::getPerms).ne(SysPermission::getPerms, "").list().stream()
                    .map(SysPermission::getPerms).collect(java.util.stream.Collectors.toSet());
        }
        return baseMapper.selectPermissionCodeSetByUserId(userId);
    }

    /** 查询权限定义，目录权限不隐含目录下的操作授权。 */
    @Override
    public List<PermissionVO> listPermissions() {
        authorizationService.requirePermission("system:permission:list");
        return lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId).list().stream().map(permission -> {
            PermissionVO response = new PermissionVO();
            response.setId(permission.getId());
            response.setMenuName(permission.getMenuName());
            response.setPerms(permission.getPerms());
            response.setStatus(permission.getStatus());
            response.setDataPermission(permission.getDataPermission());
            response.setParentId(permission.getParentId());
            response.setMenuType(permission.getMenuType());
            return response;
        }).toList();
    }

    /** 校验权限管理边界，保留权限状态恢复所需的管理操作。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPermissionStatus(Long permissionId, Integer status) {
        authorizationService.requirePermission("system:permission:status");
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysPermission permission = lambdaQuery().eq(SysPermission::getId, permissionId).last("FOR UPDATE").one();
        if (permission == null) {
            throw new IllegalArgumentException("目标权限不存在");
        }
        if (!authorizationService.isSuperAdministrator()) {
            authorizationService.requireGrantablePermission(permissionId);
        }
        if (status == 1 && "system:permission:status".equals(permission.getPerms())) {
            throw new IllegalArgumentException("不能停用权限状态管理操作");
        }
        permission.setStatus(status);
        updateById(permission);
    }

    /** 数据库事务提交成功后撤销受影响账号的旧授权快照。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysPermission entity) {
        Set<Long> affected = baseMapper.selectUserIdSetByPermissionId(entity.getId());
        boolean updated = super.updateById(entity);
        if (updated) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affected);
        }
        return updated;
    }
}

