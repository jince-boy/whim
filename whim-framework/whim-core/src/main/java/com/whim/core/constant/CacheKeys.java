package com.whim.core.constant;

import lombok.NoArgsConstructor;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 缓存键常量
 */
@NoArgsConstructor
public final class CacheKeys {
    /**
     * 内置配置和自定义配置的业务值。
     */
    public static final String SYS_CONFIG = "sys_config_values";

    /**
     * 系统字典缓存。
     */
    public static final String SYS_DICT = "sys_dict";

    /**
     * 系统字典类型缓存。
     */
    public static final String SYS_DICT_TYPE = "sys_dict_type";
}
