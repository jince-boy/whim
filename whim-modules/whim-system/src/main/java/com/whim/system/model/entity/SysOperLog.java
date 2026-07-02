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
 * @description 操作日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperLog extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 650015566521618061L;

    /**
     * id
     */
    private Long id;

    /**
     * 日志标题
     */
    private String title;

    /**
     * 日志类型(0插入，1更新，2删除，3导出，4导入，5其他)
     */
    private Integer logType;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 主机地点
     */
    private String operIp;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数
     */
    private Object requestParam;

    /**
     * 响应参数
     */
    private Object responseParam;

    /**
     * 操作状态
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 耗时
     */
    private Long costTime;

}

