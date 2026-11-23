package com.whim.satoken.session;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.OnlineSessionInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.satoken.constants.AuthSessionKeys;
import com.whim.satoken.security.StpAuthManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 基于 Sa-Token 的认证会话实现。
 */
@Slf4j
@RequiredArgsConstructor
public class SaTokenAuthenticationSession implements AuthenticationSession {
    private final OnlineSessionIndex sessionIndex;

    /**
     * 按账号体系查询仍可通过登录校验的在线会话。
     *
     * @param loginType 账号体系
     * @return 只读会话流，由调用方关闭
     */
    @Override
    public Stream<OnlineSessionInfo> getOnlineSessions(String loginType) {
        return sessionIndex.getOnlineSessions(StpAuthManager.getStpLogic(loginType));
    }

    /**
     * 创建用户登录会话并保存认证上下文。
     *
     * @param userInfo 用户认证信息
     * @return 登录令牌信息
     */
    @Override
    public AuthenticationToken login(UserInfo userInfo) {
        StpLogic stpLogic = StpAuthManager.getStpLogic(userInfo.getLoginType());
        stpLogic.login(userInfo.getUserId());
        String tokenValue = stpLogic.getTokenValue();
        // 动态码消费与登录可能处于同一数据库事务；回滚时精准撤销这次新签发的独立令牌。
        if (!stpLogic.isSupportShareToken() && TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 * 撤销未成功提交的登录会话。
                 *
                 * @param status 数据库事务完成状态
                 */
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        stpLogic.logoutByTokenValue(tokenValue);
                    }
                }
            });
        }
        SaSession session = stpLogic.getTokenSession();
        ServletUtils.ClientAgentInfo client = ServletUtils.getClientAgentInfo();
        // 终端信息只采集于登录时，与身份快照一次写入；后续刷新用户资料不得覆盖登录现场。
        String ipAddress = IPUtils.getClientIpAddress();
        if (client.browserClass() != null) {
            session.getDataMap().put(AuthSessionKeys.LOGIN_CLIENT_TYPE, client.browserClass());
        }
        if (client.deviceClass() != null) {
            session.getDataMap().put(AuthSessionKeys.LOGIN_DEVICE_TYPE, client.deviceClass());
        }
        if (ipAddress != null) {
            session.getDataMap().put(AuthSessionKeys.LOGIN_IP, ipAddress);
        }
        if (client.browser() != null) {
            session.getDataMap().put(AuthSessionKeys.LOGIN_BROWSER, client.browser());
        }
        if (client.operatingSystem() != null) {
            session.getDataMap().put(AuthSessionKeys.LOGIN_OS, client.operatingSystem());
        }
        session.set(AuthSessionKeys.LOGIN_USER, userInfo);
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setTokenType(stpLogic.getConfigOrGlobal().getTokenPrefix());
        authenticationToken.setAccessToken(tokenValue);
        authenticationToken.setExpiresIn(stpLogic.getTokenTimeout());
        return authenticationToken;
    }

    /**
     * 更新当前令牌的用户认证上下文。
     *
     * @param userInfo 最新用户认证信息
     */
    @Override
    public void updateUserInfo(UserInfo userInfo) {
        StpLogic stpLogic = getRequiredCurrentStpLogic();
        if (!Objects.equals(stpLogic.getLoginIdAsLong(), userInfo.getUserId())) {
            throw new IllegalArgumentException("不能修改其他用户的认证上下文");
        }
        stpLogic.getTokenSession().set(AuthSessionKeys.LOGIN_USER, userInfo);
    }

    /**
     * 注销当前登录会话。
     */
    @Override
    public void logout() {
        getRequiredCurrentStpLogic().logout();
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
        log.info("已强制用户会话失效，loginType={}，userCount={}", loginType, userIds.size());
    }

    /**
     * 在目标账号的终端中定位会话，核实所有者后仅撤销匹配令牌。
     *
     * @param loginType 账号体系
     * @param userId 目标账号ID
     * @param sessionId 公开会话标识
     */
    @Override
    public void kickoutSession(String loginType, Long userId, String sessionId) {
        StpLogic logic = StpAuthManager.getStpLogic(loginType);
        for (String token : logic.getTokenValueListByLoginId(userId)) {
            if (!DigestUtils.sha256Hex(token).equals(sessionId)) {
                continue;
            }
            // 当前会话只能走主动退出；旧行在刷新前过期或被替换时，不影响后续登录。
            if (token.equals(logic.getTokenValue())) {
                throw new AccessDeniedException("不能强制下线当前会话");
            }
            if (userId.toString().equals(String.valueOf(logic.getLoginIdByToken(token)))) {
                logic.kickoutByTokenValue(token);
            }
            return;
        }
    }

    /**
     * 在事务提交后强制用户下线；没有事务时立即执行。
     *
     * @param loginType 账号体系
     * @param userIds   用户ID集合
     */
    @Override
    public void kickoutAfterCommit(String loginType, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Set<Long> affectedUserIds = Set.copyOf(userIds);
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            kickout(loginType, affectedUserIds);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /** 事务提交成功后使旧会话失效。 */
            @Override
            public void afterCommit() {
                kickout(loginType, affectedUserIds);
            }
        });
    }

    /**
     * 获取当前请求对应的 Sa-Token 账号体系。
     *
     * @return 当前账号体系的 StpLogic
     */
    private StpLogic getRequiredCurrentStpLogic() {
        StpLogic stpLogic = StpAuthManager.getCurrentStpLogic();
        if (Objects.isNull(stpLogic)) {
            throw new IllegalStateException("当前请求未登录，无法操作认证会话");
        }
        return stpLogic;
    }
}
