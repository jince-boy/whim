package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统权限菜单表服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    /**
     * 认证会话操作对象
     */
    private final AuthenticationSession authenticationSession;

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

    /**
     * 修改权限，并强制当前拥有该权限的用户退出登录。
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
            authenticationSession.kickout(AuthUserType.SYSTEM, affectedUserIds);
            log.info("权限修改后已强制相关用户下线，permissionId={}，userCount={}",
                    entity.getId(), affectedUserIds.size());
        }
        return updated;
    }
}

