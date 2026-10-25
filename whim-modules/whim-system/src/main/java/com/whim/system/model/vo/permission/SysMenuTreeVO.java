package com.whim.system.model.vo.permission;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 菜单选择树节点，前端按menuType选择允许的父节点。
 */
@Data
public class SysMenuTreeVO {
    /** 菜单ID，可作为树组件的键。 */
    private Long id;
    /** 显示名称。 */
    private String label;
    /** 菜单类型。 */
    private Integer menuType;
    /** 状态：0正常、1停用，停用节点保留用于授权维护。 */
    private Integer status;
    /** 子节点。 */
    private List<SysMenuTreeVO> children = new ArrayList<>();
}
