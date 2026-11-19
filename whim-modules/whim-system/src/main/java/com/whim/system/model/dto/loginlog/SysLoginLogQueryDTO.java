package com.whim.system.model.dto.loginlog;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.log.enums.LogStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 登录日志筛选及分页条件。
 */
@Getter
@Setter
public class SysLoginLogQueryDTO extends PageQueryDTO {
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;

    @Size(max = 45, message = "IP地址不能超过45个字符")
    private String ipAddress;

    private Integer status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * 校验筛选条件中的执行结果编码，允许查询全部结果。
     *
     * @return 登录结果筛选条件是否合法
     */
    @AssertTrue(message = "登录结果无效")
    public boolean isStatusValid() {
        return status == null || Arrays.stream(LogStatus.values()).anyMatch(result -> result.getCode() == status);
    }

    /**
     * 校验登录时间区间的起止顺序。
     *
     * @return 时间区间是否合法
     */
    @AssertTrue(message = "开始时间不能晚于结束时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }
}
