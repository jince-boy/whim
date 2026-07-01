package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.satoken.constants.AuthUserType;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 系统用户表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;

    /**
     * 判断是否支持系统账号体系。
     *
     * @param loginType Sa-Token账号体系标识
     * @return true表示支持系统账号体系
     */
    @Override
    public boolean supports(String loginType) {
        return AuthUserType.SYSTEM.equals(loginType);
    }

    /**
     * 获取用户权限编码集合。
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    @Override
    public Set<String> getPermissionList(Long userId) {
        return sysPermissionService.getPermissionCodeSetByUserId(userId);
    }

    /**
     * 获取用户角色编码集合。
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    @Override
    public Set<String> getRoleList(Long userId) {
        return sysRoleService.getRoleCodeSetByUserId(userId);
    }
}
