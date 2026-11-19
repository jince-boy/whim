package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统登录日志。
 */
@Getter
@Setter
public class SysLoginLog extends BaseEntity {
    private Long id;
    private String username;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private String deviceType;
    private Integer status;
    private String remark;
    private LocalDateTime loginTime;
    @TableLogic
    private Long deleted;
}
