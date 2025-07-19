package com.whim.satoken.core.context;

import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.whim.satoken.core.logic.StpAuthManager;
import com.whim.satoken.core.model.UserInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author jince
 * @date 2025/7/7 23:22
 * @description 认证上下文
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthContext {
    /**
     * 登录
     */
    public static void login(UserInfo userInfo, SaLoginParameter saLoginParameter) {
        saLoginParameter = ObjectUtils.defaultIfNull(saLoginParameter, new SaLoginParameter());
        StpAuthManager.SYSTEM.login(userInfo.getUserId(), saLoginParameter);
        StpAuthManager.SYSTEM.getSession().set("userInfo", userInfo);
    }

    /**
     * 获取用户信息
     */
    public static UserInfo getUserInfo() {
        return (UserInfo) StpAuthManager.SYSTEM.getSession().get("userInfo");
    }

    /**
     * 判断是否是超级管理员
     */
    public static boolean isSuperAdmin(Long userId) {
        return userId.equals(1L);
    }

    /**
     * 判断是否是超级管理员
     */
    public static boolean isSuperAdmin() {
        return isSuperAdmin(StpAuthManager.SYSTEM.getLoginIdAsLong());
    }
}
