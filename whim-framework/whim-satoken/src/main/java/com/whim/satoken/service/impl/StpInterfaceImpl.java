package com.whim.satoken.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.core.utils.SpringUtils;
import com.whim.satoken.core.common.AccountType;
import com.whim.satoken.core.context.AuthContext;
import com.whim.satoken.core.model.UserInfo;
import com.whim.satoken.service.PermissionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jince
 * @date 2025/6/19 15:48
 * @description 登录权限服务
 */
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object userId, String loginType) {
        if (loginType.equals(AccountType.SYSTEM)) {
            UserInfo userInfo = AuthContext.getUserInfo();
            if (Objects.nonNull(userInfo)) {
                return new ArrayList<>(AuthContext.getUserInfo().getPermissionCodeSet());
            } else {
                long sysUserId = Long.parseLong(userId.toString());
                return new ArrayList<>(getSystemPermission().getPermissionList(sysUserId));
            }
        }
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object userId, String loginType) {
        // 后台用户权限认证
        if (loginType.equals(AccountType.SYSTEM)) {
            UserInfo userInfo = AuthContext.getUserInfo();
            if (Objects.nonNull(userInfo)) {
                return new ArrayList<>(AuthContext.getUserInfo().getRoleCodeSet());
            } else {
                long sysUserId = Long.parseLong(userId.toString());
                return new ArrayList<>(getSystemPermission().getRoleList(sysUserId));
            }
        }
        return List.of();
    }

    public PermissionProvider getSystemPermission() {
        return SpringUtils.getBean("systemPermission", PermissionProvider.class);
    }
}
