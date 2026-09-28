package com.whim.system.service;

import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 在授权数据提交后使受影响的系统账号会话失效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationSessionInvalidator {
    private final AuthenticationSession authenticationSession;

    /**
     * 在当前事务提交后踢出用户；没有事务时立即执行。
     *
     * @param userIds 受影响用户ID
     */
    public void kickoutAfterCommit(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Set<Long> affectedUserIds = Set.copyOf(userIds);
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            kickout(affectedUserIds);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /** 提交成功后撤销旧会话中的授权快照。 */
            @Override
            public void afterCommit() {
                kickout(affectedUserIds);
            }
        });
    }

    /**
     * 强制受影响用户重新登录以取得最新授权。
     *
     * @param userIds 用户ID
     */
    private void kickout(Set<Long> userIds) {
        authenticationSession.kickout(AuthUserType.SYSTEM, userIds);
        log.info("授权发生变化，已使相关账号会话失效，userCount={}", userIds.size());
    }
}
