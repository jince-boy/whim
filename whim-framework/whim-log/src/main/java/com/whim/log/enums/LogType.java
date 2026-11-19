package com.whim.log.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 操作类型编码，对应操作类型字典。
 */
@Getter
@RequiredArgsConstructor
public enum LogType {
    INSERT(0),
    UPDATE(1),
    DELETE(2),
    EXPORT(3),
    IMPORT(4),
    OTHER(5),
    CLEAN(6);

    public static final String DICTIONARY_TYPE = "sys_oper_type";

    private final int code;
}
