package com.whim.log.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统执行结果编码，展示标签和样式由执行结果字典维护。
 */
@Getter
@RequiredArgsConstructor
public enum LogStatus {
    SUCCESS(0),
    FAILURE(1);

    public static final String DICTIONARY_TYPE = "sys_result_status";

    private final int code;
}
