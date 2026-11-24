package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统操作日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperLog extends BaseEntity {
    private Long id;
    private String title;
    private Integer logType;
    private String methodName;
    private String requestMethod;
    private String operName;
    private String operIp;
    private String operLocation;
    private String requestUrl;
    @ToString.Exclude
    private String requestParam;
    @ToString.Exclude
    private String responseParam;
    private Integer status;
    @ToString.Exclude
    private String errorMessage;
    private LocalDateTime operTime;
    private Long costTime;
    @TableLogic
    private Long deleted;
}
