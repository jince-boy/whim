package com.whim.system.model.vo.operlog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.whim.core.utils.IPUtils;
import com.whim.system.model.entity.SysOperLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 操作日志列表和导出使用的审计字段。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysOperLog.class, convertGenerate = false)
public class SysOperLogVO {
    @ExcelProperty(value = "日志ID", index = 0)
    private Long id;
    @ExcelProperty(value = "模块名称", index = 1)
    private String title;
    @ExcelProperty(value = "操作人", index = 2)
    private String operName;
    private Integer logType;
    @JsonIgnore
    @ExcelProperty(value = "操作类型", index = 3)
    private String typeLabel;
    @ExcelProperty(value = "操作IP", index = 4)
    private String operIp;
    @ExcelProperty(value = "操作地点", index = 5)
    private String operLocation;
    private Integer status;
    @JsonIgnore
    @ExcelProperty(value = "操作结果", index = 6)
    private String statusLabel;
    @ExcelProperty(value = "操作时间", index = 7)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;
    @ExcelProperty(value = "耗时（毫秒）", index = 8)
    private Long costTime;

    /**
     * 获取操作日志中可读的 IP 归属地。
     *
     * @return 操作地点
     */
    public String getOperLocation() {
        return IPUtils.formatRecordedLocation(operIp, operLocation);
    }
}
