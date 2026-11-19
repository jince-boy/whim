package com.whim.system.model.vo.loginlog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.whim.system.model.entity.SysLoginLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 登录日志列表和导出使用的审计字段。
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysLoginLog.class, convertGenerate = false)
public class SysLoginLogVO {
    @ExcelProperty(value = "日志ID", index = 0)
    private Long id;
    @ExcelProperty(value = "用户名", index = 1)
    private String username;
    @ExcelProperty(value = "登录IP", index = 2)
    private String ipAddress;
    @ExcelProperty(value = "登录地点", index = 3)
    private String loginLocation;
    @ExcelProperty(value = "浏览器", index = 4)
    private String browser;
    @ExcelProperty(value = "操作系统", index = 5)
    private String os;
    @ExcelProperty(value = "设备类型", index = 6)
    private String deviceType;
    private Integer status;
    @ExcelProperty(value = "结果说明", index = 8)
    private String remark;
    @ExcelProperty(value = "登录时间", index = 9)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    @JsonIgnore
    @ExcelProperty(value = "登录结果", index = 7)
    private String statusLabel;
}
