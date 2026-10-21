package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号查询及角色、权限信息构建。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;

    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public SysUser getUserById(Long id) {
        return this.lambdaQuery()
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                .eq(SysUser::getId, id)
                .one();
    }

    /**
     * 用户名全局规范化，登录阶段不依赖业务数据权限。
     */
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username.trim().toLowerCase(Locale.ROOT)).one();
    }

    /**
     * 构建登录快照，角色权限关系同时用于功能授权和数据范围计算。
     */
    @Override
    public UserInfo buildUserInfo(SysUser user) {
        UserInfo info = BeanConvertUtils.convert(user, UserInfo.class);
        info.setLoginType(AuthUserType.SYSTEM);
        List<RoleInfo> roles = roleService.getRoleInfoListByUserId(user.getId());
        info.setRoleInfoList(roles);
        info.setRoleCodeSet(roles.stream().map(RoleInfo::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        info.setPermissionRoleIds(permissionService.getPermissionRoleIds(roles));
        info.setPermissionCodeSet(new LinkedHashSet<>(info.getPermissionRoleIds().keySet()));
        return info;
    }
}
