package com.whim.satoken.context;

import cn.dev33.satoken.stp.StpLogic;
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
    public static final String LOGIN_USER_SESSION_KEY = "userInfo";
    private static final String SUPER_ADMINISTRATOR_ROLE_CODE = "superadmin";

    /**
     * 完成登录并写入认证上下文。
     *
     * @param userInfo 登录用户上下文信息
     */
    public void login(UserInfo userInfo) {
        StpLogic stpLogic = StpAuthManager.getStpLogic(userInfo.getLoginType());
        stpLogic.login(userInfo.getUserId());
        stpLogic.getSession().set(LOGIN_USER_SESSION_KEY, userInfo);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Override
    public UserInfo getCurrentUserInfo() {
        UserInfo userInfo = getCurrentUserInfoOrNull();
        if (Objects.isNull(userInfo)) {
            throw new IllegalStateException("当前登录上下文缺少用户信息");
        }
        return userInfo;
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 当前登录用户ID
     */
    @Override
    public Long getUserId() {
        UserInfo userInfo = getCurrentUserInfoOrNull();
        if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getUserId())) {
            return userInfo.getUserId();
        }
        StpLogic stpLogic = getRequiredCurrentStpLogic();
        return stpLogic.getLoginIdAsLong();
    }

    /**
     * 获取当前登录账号体系。
     *
     * @return 当前登录账号体系
     */
    @Override
    public String getLoginType() {
        UserInfo userInfo = getCurrentUserInfoOrNull();
        if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getLoginType())) {
            return userInfo.getLoginType();
        }
        StpLogic stpLogic = getRequiredCurrentStpLogic();
        return stpLogic.getLoginType();
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
        UserInfo userInfo = getCurrentUserInfoOrNull();
        if (Objects.nonNull(userInfo)) {
            return userInfo.getRoleCodeSet().contains(SUPER_ADMINISTRATOR_ROLE_CODE);
        }
        return getRequiredCurrentStpLogic().hasRoleOr(SUPER_ADMINISTRATOR_ROLE_CODE);
    }

    /**
     * 获取当前请求所属的 Sa-Token 账号体系。
     *
     * @return 当前请求所属的 Sa-Token 账号体系
     */
    private StpLogic getRequiredCurrentStpLogic() {
        StpLogic stpLogic = StpAuthManager.getCurrentStpLogic();
        if (Objects.isNull(stpLogic)) {
            throw new IllegalStateException("当前请求未登录，无法获取认证上下文");
        }
        return stpLogic;
    }

    /**
     * 获取当前登录用户信息，缺失时返回 null。
     *
     * @return 当前登录用户信息
     */
    private UserInfo getCurrentUserInfoOrNull() {
        if (!isLogin()) {
            return null;
        }
        Object userInfo = getRequiredCurrentStpLogic().getSession().get(LOGIN_USER_SESSION_KEY);
        if (userInfo instanceof UserInfo currentUserInfo) {
            return currentUserInfo;
        }
        return null;
    }
}
