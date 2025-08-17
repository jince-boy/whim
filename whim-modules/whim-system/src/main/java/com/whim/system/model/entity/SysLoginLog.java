package com.whim.system.model.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统登录日志(SysLoginLog)实体类
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@Data
public class SysLoginLog implements Serializable {
    @Serial
    private static final long serialVersionUID = -93985843605044778L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 浏览器
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 登录地点
     */
    private String loginLocation;
    /**
     * ip地址
     */
    private String ipAddress;
    /**
     * 登录状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
