package com.whim.core.auth;

import com.whim.core.auth.model.UserInfo;

/**
 * @author Jince
 * @date 2026/04/12
 * @description 当前请求认证上下文抽象，统一提供当前登录用户信息的访问入口。
 */
public interface AuthenticationContext {
    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    UserInfo getCurrentUserInfo();

    /**
     * 判断当前请求是否已经完成登录认证。
     *
     * @return true 表示当前请求已登录
     */
    boolean isLogin();

    /**
     * 判断当前登录用户是否为超级管理员。
     *
     * @return true 表示当前用户是超级管理员
     */
    boolean isSuperAdministrator();
}
