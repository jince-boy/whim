package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 操作日志实体类
 */
@Data
public class SysOperLog implements Serializable {
    @Serial
    private static final long serialVersionUID = -22061317923986590L;

    /**
     * id
     */
    @TableId(value = "id")
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

