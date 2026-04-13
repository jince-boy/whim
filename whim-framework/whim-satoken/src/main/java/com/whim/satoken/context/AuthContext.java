package com.whim.satoken.context;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.security.StpAuthManager;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/13
 * @description 基于 Sa-Token 的认证上下文实现。
 * 通过 {@link StpAuthManager} 动态识别当前请求所属的账号体系，避免与具体体系常量耦合。
 */
public class AuthContext implements AuthenticationContext {

    /**
     * 获取当前登录用户信息。
     * 从当前请求所属账号体系的 Session 中读取登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Override
    public UserInfo getCurrentUserInfo() {
        if (!isLogin()) {
            throw new IllegalStateException("当前请求未登录，无法获取用户信息");
        }
        return (UserInfo) Objects.requireNonNull(StpAuthManager.getCurrentStpLogic()).getSession().get("userInfo");
    }

    /**
     * 判断当前请求是否已完成登录认证。
     *
     * @return true 表示当前请求已登录
     */
    @Override
    public boolean isLogin() {
        return StpAuthManager.isLogin();
    }

    /**
     * 判断当前登录用户是否为超级管理员。
     *
     * @return true 表示当前用户是超级管理员
     */
    @Override
    public boolean isSuperAdministrator() {
        if (!isLogin()) {
            return false;
        }
        return Objects.requireNonNull(StpAuthManager.getCurrentStpLogic()).hasRoleOr("superadmin");
    }
}
