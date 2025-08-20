package com.whim.log.enums;

import lombok.Getter;

/**
 * @author jince
 * date: 2025/8/18 16:49
 * description: 日志类型
 */
@Getter
public enum LogType {
    INSERT(0),
    UPDATE(1),
    DELETE(2),
    EXPORT(3),
    IMPORT(4),
    OTHER(5),
    CLEAN(6);
    private final int code;

    LogType(int code) {
        this.code = code;
    }
}
