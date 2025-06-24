package com.whim.satoken.service;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.core.utils.SpringUtils;
import com.whim.satoken.kit.StpKit;
import com.whim.satoken.provider.IAccountAuthProvider;

import java.util.List;

/**
 * @author jince
 * date: 2025/6/19 15:48
 * description: 登录权限服务
 */
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object userId, String loginType) {
        if (loginType.equals(StpKit.AccountType.SYSTEM)) {
            long sysUserId = Long.parseLong(userId.toString());
            if (sysUserId == 1L) {
                return List.of("*");
            }
            return getSystemPermission().getPermissionList(sysUserId);
        }
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object userId, String loginType) {
        // 后台用户权限认证
        if (loginType.equals(StpKit.AccountType.SYSTEM)) {
            return getSystemPermission().getRoleList((Long) userId);
        }
        return List.of();
    }

    public IAccountAuthProvider getSystemPermission() {
        return SpringUtils.getBean("system", IAccountAuthProvider.class);
    }
}
