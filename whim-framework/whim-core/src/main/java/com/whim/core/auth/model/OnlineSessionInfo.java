package com.whim.core.auth.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 在线会话及其在服务端登录快照中的归属。
 */
@Data
public class OnlineSessionInfo {
    private String sessionId;
    private Long userId;
    private Long deptId;
    private String username;
    /** 登录请求的客户端软件类别。 */
    private String clientType;
    /** 登录请求的设备类别，与登录日志采用相同口径。 */
    private String deviceType;
    private String name;
    private String deptName;
    private String ipAddress;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private boolean current;
}
