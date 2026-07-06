package com.whim.satoken.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.context.AuthContext;
import com.whim.satoken.security.StpAuthManager;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/07/01
 * @description Sa-Token 权限验证适配器
 */
public class StpInterfaceImpl implements StpInterface {
    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserInfo userInfo = getUserInfo(loginType);
        if (Objects.isNull(userInfo)) {
            return List.of();
        }
        return List.copyOf(userInfo.getPermissionCodeSet());
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserInfo userInfo = getUserInfo(loginType);
        if (Objects.isNull(userInfo)) {
            return List.of();
        }
        return List.copyOf(userInfo.getRoleCodeSet());
    }

    /**
     * 从当前 Sa-Token 会话中读取登录用户上下文。
     *
     * @param loginType 账号类型
     * @return 登录用户上下文
     */
    private UserInfo getUserInfo(String loginType) {
        Object userInfo = StpAuthManager.getStpLogic(loginType).getSession().get(AuthContext.LOGIN_USER_SESSION_KEY);
        if (userInfo instanceof UserInfo currentUserInfo) {
            return currentUserInfo;
        }
        return null;
    }
}
