package com.whim.system.model.dto.sysOperLog;

import com.whim.system.model.entity.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/8/19 22:43
 * description: 系统操作日志查询参数
 */
@AutoMapper(target = SysOperLog.class)
@Data
public class SysOperLogQueryDTO {
    /**
     * 日志标题
     */
    private String title;
    /**
     * 日志类型(0插入，1更新，2删除，3导出，4导入，5其他)
     */
    private Integer logType;
    /**
     * 操作人员
     */
    private String operName;
    /**
     * 主机地点
     */
    private String operIp;
    /**
     * 操作状态
     */
    private Integer status;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
