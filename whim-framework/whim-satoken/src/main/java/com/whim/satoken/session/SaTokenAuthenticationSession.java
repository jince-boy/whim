package com.whim.satoken.session;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.constants.AuthSessionKeys;
import com.whim.satoken.security.StpAuthManager;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 基于 Sa-Token 的认证会话实现。
 */
public class SaTokenAuthenticationSession implements AuthenticationSession {

    /**
     * 创建用户登录会话并保存认证上下文。
     *
     * @param userInfo  用户认证信息
     * @param rememberMe 是否持久化客户端登录状态
     * @return 登录令牌信息
     */
    @Override
    public AuthenticationToken login(UserInfo userInfo, boolean rememberMe) {
        StpLogic stpLogic = StpAuthManager.getStpLogic(userInfo.getLoginType());
        SaLoginParameter loginParameter = stpLogic.createSaLoginParameter()
                .setIsLastingCookie(rememberMe);
        stpLogic.login(userInfo.getUserId(), loginParameter);
        stpLogic.getSession().set(AuthSessionKeys.LOGIN_USER, userInfo);

        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setTokenType(stpLogic.getConfigOrGlobal().getTokenPrefix());
        authenticationToken.setAccessToken(stpLogic.getTokenValue());
        authenticationToken.setExpiresIn(stpLogic.getTokenTimeout());
        return authenticationToken;
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        StpLogic stpLogic = StpAuthManager.getCurrentStpLogic();
        if (Objects.isNull(stpLogic)) {
            throw new IllegalStateException("当前请求未登录，无法注销认证会话");
        }
        stpLogic.logout();
    }

    /**
     * 强制指定账号体系下的用户退出全部登录会话。
     *
     * @param loginType 账号体系
     * @param userIds   用户ID集合
     */
    @Override
    public void kickout(String loginType, Collection<Long> userIds) {
        StpLogic stpLogic = StpAuthManager.getStpLogic(loginType);
        userIds.forEach(stpLogic::kickout);
    }
}
