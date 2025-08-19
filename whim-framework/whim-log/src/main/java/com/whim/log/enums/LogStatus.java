package com.whim.log.enums;

import lombok.Getter;

/**
 * @author jince
 * date: 2025/8/18 16:58
 * description: 日志状态
 */
@Getter
public enum LogStatus {
    SUCCESS(0),
    FAILURE(1);
    private final int code;

    LogStatus(int code) {
        this.code = code;
    }
}
