package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统登录日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginLog extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 184278338366294316L;

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

