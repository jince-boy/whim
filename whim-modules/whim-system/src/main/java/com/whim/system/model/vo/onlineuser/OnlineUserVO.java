package com.whim.system.model.vo.onlineuser;

import com.whim.core.auth.model.OnlineSessionInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 可见在线会话和当前操作者的强退能力。
 */
@Data
@AutoMapper(target = OnlineSessionInfo.class, convertGenerate = false)
public class OnlineUserVO {
    private String sessionId;
    private Long userId;
    private String username;
    /** 登录请求的客户端软件类别。 */
    private String clientType;
    /** 登录请求的设备类别，与登录日志采用相同口径。 */
    private String deviceType;
    private String name;
    private String deptName;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private boolean current;
    private boolean canKickout;
}
