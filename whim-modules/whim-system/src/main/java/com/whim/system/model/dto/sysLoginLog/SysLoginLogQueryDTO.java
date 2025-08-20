package com.whim.system.model.dto.sysLoginLog;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/8/20 11:17
 * description: 登录日志查询参数
 */
@Data
public class SysLoginLogQueryDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 登录状态
     */
    private Integer status;

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
