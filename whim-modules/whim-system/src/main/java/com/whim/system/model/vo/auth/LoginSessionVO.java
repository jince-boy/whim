package com.whim.system.model.vo.auth;

import com.whim.core.auth.model.OnlineSessionInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 个人信息页展示的登录终端。
 */
@Data
@AutoMapper(target = OnlineSessionInfo.class, convertGenerate = false)
public class LoginSessionVO {
    private String sessionId;
    private String deviceType;
    private String browser;
    private String os;
    private String ipAddress;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private boolean current;
}
