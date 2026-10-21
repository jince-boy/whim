package com.whim.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/10/15
 * @description 系统权限节点类型。
 */
@Getter
@RequiredArgsConstructor
public enum SysMenuType {

    DIRECTORY(1, "目录"),
    PAGE(2, "页面"),
    BUTTON(3, "按钮"),
    EXTERNAL_LINK(4, "外链");

    private final Integer code;
    private final String description;

}
