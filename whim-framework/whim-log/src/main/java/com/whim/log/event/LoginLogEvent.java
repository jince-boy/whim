package com.whim.log.event;

import com.whim.log.enums.LogStatus;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 认证结果确定时采集的登录日志事件。
 */
@Value
public class LoginLogEvent {
    String username;
    String ipAddress;
    String userAgent;
    LocalDateTime loginTime;
    LogStatus status;
    String remark;
}
