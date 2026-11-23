package com.whim.log.publisher;

import com.whim.core.utils.ServletUtils;
import com.whim.log.enums.LogStatus;
import com.whim.log.event.LoginLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 采集登录来源并发布认证结果事件。
 */
@Slf4j
@RequiredArgsConstructor
public class LoginLogPublisher {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 在认证请求结束前固化来源，投递失败时保留原认证结果。
     *
     * @param username 本次认证的用户名
     * @param clientAddress 认证限流使用的客户端地址
     * @param status 登录结果
     * @param remark 本次认证的结果说明
     */
    public void publishLoginLog(String username, String clientAddress, LogStatus status, String remark) {
        publishLoginLog(username, clientAddress, status, remark, null);
    }

    /**
     * 记录认证结果，并为已知账号的凭据错误提供安全提醒所需的收件人标识。
     *
     * @param username 本次认证的用户名
     * @param clientAddress 认证来源地址
     * @param status 登录结果
     * @param remark 认证结果说明
     * @param credentialFailureUserId 凭据错误对应的账号，其他情况为空
     */
    public void publishLoginLog(String username, String clientAddress, LogStatus status, String remark, Long credentialFailureUserId) {
        try {
            eventPublisher.publishEvent(new LoginLogEvent(username, clientAddress,
                    ServletUtils.getUserAgentHeader().orElse(null), LocalDateTime.now().withNano(0), status, remark, credentialFailureUserId));
        } catch (RuntimeException exception) {
            log.error("登录日志事件投递失败，客户端地址={}", clientAddress, exception);
        }
    }
}
