package com.whim.satoken.session;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.constants.AuthSessionKeys;
import com.whim.satoken.security.StpAuthManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 基于 Sa-Token 的认证会话实现。
 */
@Slf4j
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
        stpLogic.getTokenSession().set(AuthSessionKeys.LOGIN_USER, userInfo);

        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setTokenType(stpLogic.getConfigOrGlobal().getTokenPrefix());
        authenticationToken.setAccessToken(stpLogic.getTokenValue());
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
