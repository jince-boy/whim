package com.whim.core.auth;

import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.UserInfo;

import java.util.Collection;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 认证会话操作抽象，统一提供登录、退出、令牌创建与强制下线能力。
 */
public interface AuthenticationSession {

    /**
     * 创建用户登录会话。
     *
     * @param userInfo  用户认证信息
     * @param rememberMe 是否持久化客户端登录状态
     * @return 登录令牌信息
     */
    AuthenticationToken login(UserInfo userInfo, boolean rememberMe);

    /**
     * 注销当前登录会话。
     */
    void logout();

    /**
     * 强制指定账号体系下的用户退出全部登录会话。
     *
     * @param loginType 账号体系
     * @param userIds   用户ID集合
     */
    void kickout(String loginType, Collection<Long> userIds);
}
