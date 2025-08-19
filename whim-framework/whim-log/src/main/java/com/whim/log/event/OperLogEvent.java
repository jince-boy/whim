package com.whim.log.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/8/19 15:55
 * description: 操作日志事件
 */
@Data
public class OperLogEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志标题
     */
    private String title;
    /**
     * 日志类型
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
     * 操作人员IP
     */
    private String operIp;
    /**
     * 请求地址
     */
    private String requestUrl;
    /**
     * 操作地点
     */
    private String operLocation;
    /**
     * 请求参数
     */
    private String requestParam;
    /**
     * 返回参数
     */
    private String responseParam;
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
