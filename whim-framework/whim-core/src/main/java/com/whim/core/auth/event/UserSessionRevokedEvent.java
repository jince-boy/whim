package com.whim.core.auth.event;

import lombok.Value;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 通知长连接重新校验指定账号的会话。
 */
@Value
public class UserSessionRevokedEvent {
    String loginType;
    Long userId;
}
