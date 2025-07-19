package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.satoken.core.context.AuthContext;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.entity.SysRole;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * @description 系统角色(SysRole)表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final SysRoleMapper sysRoleMapper;

    /**
     * 根据用户ID获取角色权限列表
     *
     * @param userId 用户ID
     * @return 角色权限列表
     */
    @Override
    public Set<String> getRoleCodeByUserId(Long userId) {
        if (AuthContext.isSuperAdmin(userId)) {
            return Set.of("*");
        } else {
            return sysRoleMapper.getRoleCodeByUserId(userId);
        }
    }

    /**
     * 根据用户id获取角色信息列表
     *
     * @param userId 用户id
     * @return 角色信息列表
     */
    @Override
    public List<SysRole> getRoleInfoListByUserId(Long userId) {
        return sysRoleMapper.getRoleInfoListByUserId(userId);
    }
}

