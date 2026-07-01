package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.service.ISysPermissionService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 权限表(菜单和按钮权限)服务实现类
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    /**
     * 查询用户已启用权限编码集合。
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    @Override
    public Set<String> getPermissionCodeSetByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return Set.of();
        }
        return Objects.requireNonNullElse(baseMapper.selectPermissionCodeSetByUserId(userId), Set.of());
    }
}
