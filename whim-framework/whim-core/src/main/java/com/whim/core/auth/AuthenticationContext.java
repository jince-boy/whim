package com.whim.core.auth;

import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/04/12
 * @description 当前请求认证上下文抽象，统一提供跨模块需要的登录身份与权限信息。
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

    /**
     * 获取当前登录用户ID。
     *
     * @return 当前登录用户ID
     */
    default Long getUserId() {
        return getCurrentUserInfo().getUserId();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 当前登录用户名
     */
    default String getUsername() {
        return getCurrentUserInfo().getUsername();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 当前登录用户名
     */
    default String getUserName() {
        return getUsername();
    }

    /**
     * 获取当前登录用户所属部门ID。
     *
     * @return 当前登录用户所属部门ID
     */
    default Long getDeptId() {
        return getCurrentUserInfo().getDeptId();
    }

    /**
     * 获取当前登录账号体系。
     *
     * @return 当前登录账号体系
     */
    default String getLoginType() {
        return getCurrentUserInfo().getLoginType();
    }

    /**
     * 获取当前登录用户权限编码集合。
     *
     * @return 当前登录用户权限编码集合
     */
    default Set<String> getPermissionCodeSet() {
        return getCurrentUserInfo().getPermissionCodeSet();
    }

    /**
     * 获取当前登录用户角色编码集合。
     *
     * @return 当前登录用户角色编码集合
     */
    default Set<String> getRoleCodeSet() {
        return getCurrentUserInfo().getRoleCodeSet();
    }

    /**
     * 获取当前登录用户角色信息列表。
     *
     * @return 当前登录用户角色信息列表
     */
    default List<RoleInfo> getRoleInfoList() {
        return getCurrentUserInfo().getRoleInfoList();
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 当前登录用户ID
     */
    default Long getCurrentUserId() {
        return getUserId();
    }

    /**
     * 获取当前登录账号体系。
     *
     * @return 当前登录账号体系
     */
    default String getCurrentLoginType() {
        return getLoginType();
    }
}
