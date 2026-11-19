package com.whim.system.model.dto.operlog;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.log.enums.LogStatus;
import com.whim.log.enums.LogType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 操作日志筛选及分页条件。
 */
@Getter
@Setter
public class SysOperLogQueryDTO extends PageQueryDTO {
    @Size(max = 64, message = "模块名称不能超过64个字符")
    private String title;
    @Size(max = 32, message = "操作人不能超过32个字符")
    private String operName;
    private Integer logType;

    @Size(max = 45, message = "IP地址不能超过45个字符")
    private String ipAddress;

    private Integer status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * 校验筛选条件中的执行结果编码，允许查询全部结果。
     *
     * @return 操作结果筛选条件是否合法
     */
    @AssertTrue(message = "操作结果无效")
    public boolean isStatusValid() {
        return status == null || Arrays.stream(LogStatus.values()).anyMatch(result -> result.getCode() == status);
    }

    /**
     * 校验可筛选的操作类型。
     *
     * @return 操作类型是否有效
     */
    @AssertTrue(message = "操作类型无效")
    public boolean isLogTypeValid() {
        return logType == null || Arrays.stream(LogType.values()).anyMatch(type -> type.getCode() == logType);
    }

    /**
     * 校验操作时间区间的起止顺序。
     *
     * @return 时间区间是否合法
     */
    @AssertTrue(message = "开始时间不能晚于结束时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
