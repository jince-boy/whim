package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统登录日志实体类
 */
@Data
public class SysLoginLog implements Serializable {
    @Serial
    private static final long serialVersionUID = -49381699628531251L;

    /**
     * id
     */
    @TableId(value = "id")
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

