package com.whim.system.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date: 2025/7/21 12:25
 * @description: 用户信息VO
 */
@Data
public class UserInfoVO {
    /**
     * 用户信息
     */
    private SysUserVO user;
    /**
     * 权限
     */
    private Set<String> permissionCode;
    /**
     * 角色权限
     */
    private Set<String> roleCode;
    /**
     * 菜单
     */
    private List<SysPermissionVO> menus;
}
