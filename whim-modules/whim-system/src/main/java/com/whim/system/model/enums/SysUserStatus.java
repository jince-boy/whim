package com.whim.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 系统用户状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum SysUserStatus {

    /**
     * 启用
     */
    ENABLED(0, "启用"),

    /**
     * 禁用
     */
    DISABLED(1, "禁用");

    /**
     * 状态编码
     */
    private final Integer code;

    /**
     * 状态说明
     */
    private final String description;

    /**
     * 判断数据库状态编码是否与当前枚举匹配。
     *
     * @param status 状态编码
     * @return 匹配返回 true
     */
    public boolean matches(Integer status) {
        return code.equals(status);
    }
}
