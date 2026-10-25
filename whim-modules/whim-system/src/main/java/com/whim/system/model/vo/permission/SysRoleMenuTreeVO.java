package com.whim.system.model.vo.permission;

import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 角色菜单授权回显，不隐式勾选祖先或后代。
 */
@Data
public class SysRoleMenuTreeVO {
    /** 完整菜单权限选择树，包含按钮。 */
    private List<SysMenuTreeVO> menus;
    /** 实际授权的菜单ID，前端应使用独立勾选模式。 */
    private List<Long> checkedKeys;
}
