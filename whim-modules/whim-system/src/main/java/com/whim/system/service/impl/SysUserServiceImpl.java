package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.constants.AuthUserType;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;
    private final ISysUserTenantService sysUserTenantService;

    /**
     * 根据用户名查询未删除用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * 构建系统账号认证上下文。
     *
     * @param user 用户实体
     * @return 用户认证信息
     */
    @Override
    public UserInfo buildUserInfo(SysUser user) {
        Set<Long> tenantIds = sysUserTenantService.getTenantIdsByUserId(user.getId());

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setTenantIds(tenantIds);
        if (tenantIds.contains(user.getDefaultTenantId())) {
            userInfo.setDefaultTenantId(user.getDefaultTenantId());
        }
        userInfo.setLoginType(AuthUserType.SYSTEM);
        userInfo.setPermissionCodeSet(sysPermissionService.getPermissionCodeSetByUserId(user.getId()));
        userInfo.setRoleCodeSet(sysRoleService.getRoleCodeSetByUserId(user.getId()));
        userInfo.setRoleInfoList(sysRoleService.getRoleInfoListByUserId(user.getId()));
        return userInfo;
    }
}

