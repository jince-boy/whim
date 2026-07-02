package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.entity.SysRole;
import com.whim.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色表服务实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    /**
     * 查询用户已启用角色编码集合。
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    @Override
    public Set<String> getRoleCodeSetByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return Set.of();
        }
        return Objects.requireNonNullElse(baseMapper.selectRoleCodeSetByUserId(userId), Set.of());
    }
}

