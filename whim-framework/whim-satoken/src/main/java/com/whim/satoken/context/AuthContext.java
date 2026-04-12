package com.whim.satoken.context;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.security.StpAuthManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author jince
 * @date 2026/4/10
 * @description 用户认证上下文
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthContext implements AuthenticationContext {
    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Override
    public UserInfo getCurrentUserInfo() {
        return (UserInfo) StpAuthManager.SYSTEM.getSession().get("userInfo");
    }

    /**
     * 判断当前请求是否已经完成登录认证。
     *
     * @return true 表示当前请求已登录
     */
    @Override
    public boolean isLogin() {
        return StpAuthManager.SYSTEM.isLogin();
    }

    /**
     * 判断当前登录用户是否为超级管理员。
     *
     * @return true 表示当前用户是超级管理员
     */
    @Override
    public boolean isSuperAdministrator() {
        return StpAuthManager.SYSTEM.hasRoleOr("superadmin");
    }
}
