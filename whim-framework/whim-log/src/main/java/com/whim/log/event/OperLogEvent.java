package com.whim.log.event;

import com.whim.log.enums.LogStatus;
import com.whim.log.enums.LogType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 在请求线程采集的操作审计快照。
 */
@Value
@Builder
public class OperLogEvent {
    String title;
    LogType logType;
    String methodName;
    String requestMethod;
    String operName;
    String operIp;
    String requestUrl;
    String requestParam;
    String responseParam;
    LogStatus status;
    String errorMessage;
    LocalDateTime operTime;
    long costTime;
}
