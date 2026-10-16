package com.whim.system.model.enums;

import com.whim.core.exception.DataAccessDeniedException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/10/16
 * @description 角色默认范围和操作覆盖共用的数据范围类型。
 */
@Getter
@RequiredArgsConstructor
public enum SysDataScopeType {
    ALL(1),
    CUSTOM_DEPARTMENTS(2),
    OWN_DEPARTMENT(3),
    DEPARTMENT_AND_DESCENDANTS(4),
    SELF(5);

    private final int code;

    /** 解析持久化范围配置，缺失或非法配置拒绝授权。 */
    public static SysDataScopeType fromCode(Integer code) {
        if (code == null) {
            throw new DataAccessDeniedException("角色数据范围配置缺失");
        }
        return switch (code) {
            case 1 -> ALL;
            case 2 -> CUSTOM_DEPARTMENTS;
            case 3 -> OWN_DEPARTMENT;
            case 4 -> DEPARTMENT_AND_DESCENDANTS;
            case 5 -> SELF;
            default -> throw new DataAccessDeniedException("角色数据范围配置无效");
        };
    }
}
