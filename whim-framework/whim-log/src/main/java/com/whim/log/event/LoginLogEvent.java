package com.whim.log.event;

import com.whim.log.enums.LogStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 认证结果确定时采集的登录日志事件。
 */
@Value
@AllArgsConstructor
public class LoginLogEvent {
    String username;
    String ipAddress;
    String userAgent;
    LocalDateTime loginTime;
    LogStatus status;
    String remark;
    /** 仅在已知账号的密码或第二因素校验失败时赋值。 */
    Long credentialFailureUserId;

    /**
     * 创建仅用于审计的登录结果事件。
     *
     * @param username 登录用户名
     * @param ipAddress 登录来源地址
     * @param userAgent 客户端标识
     * @param loginTime 登录时间
     * @param status 认证结果
     * @param remark 认证结果说明
     */
    public LoginLogEvent(String username, String ipAddress, String userAgent, LocalDateTime loginTime, LogStatus status, String remark) {
        this(username, ipAddress, userAgent, loginTime, status, remark, null);
    }
}
