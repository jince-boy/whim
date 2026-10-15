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

    /** 判断节点类型编码是否匹配。 */
    public boolean matches(Integer menuType) {
        return code.equals(menuType);
    }

    /** 根据数据库或请求中的编码获取节点类型。 */
    public static SysMenuType fromCode(Integer code) {
        for (SysMenuType menuType : values()) {
            if (menuType.matches(code)) {
                return menuType;
            }
        }
        throw new IllegalArgumentException("菜单类型只能为1至4");
    }
}
