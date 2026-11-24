package com.whim.core.auth;

import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.OnlineSessionInfo;
import com.whim.core.auth.model.UserInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 管理账号登录会话，提供在线查询、上下文更新、退出与强制下线能力。
 */
public interface AuthenticationSession {

    /**
     * 分批读取指定账号体系仍然有效的登录会话。
     *
     * @param loginType 账号体系
     * @return 只读会话流，由调用方关闭
     */
    Stream<OnlineSessionInfo> getOnlineSessions(String loginType);

    /**
     * 获取当前账号仍然有效的登录会话。
     *
     * @return 本人的会话列表
     */
    List<OnlineSessionInfo> getCurrentUserSessions();

    /**
     * 创建用户登录会话。
     *
     * @param userInfo 用户认证信息
     * @return 登录令牌信息
     */
    AuthenticationToken login(UserInfo userInfo);

    /**
     * 更新当前令牌的用户认证上下文。
     *
     * @param userInfo 最新用户认证信息
     */
    void updateUserInfo(UserInfo userInfo);

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

    /**
     * 撤销账号的指定会话，已离线的目标可重复提交。
     *
     * @param loginType 账号体系
     * @param userId 目标用户ID
     * @param sessionId 公开会话标识
     */
    void kickoutSession(String loginType, Long userId, String sessionId);

    /**
     * 在事务提交后强制指定账号体系下的用户退出全部会话；没有事务时立即执行。
     *
     * @param loginType 账号体系
     * @param userIds   用户ID集合
     */
    void kickoutAfterCommit(String loginType, Collection<Long> userIds);
}
