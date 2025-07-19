package com.whim.satoken.service;

import java.util.Set;

/**
 * @author jince
 * @date 2025/6/19 16:11
 * @description 权限提供者，用来给不同业务模块提供权限provider
 */
public interface PermissionProvider {
    /**
     * 根据用户id获取权限列表
     *
     * @param id 用户id
     * @return 权限列表
     */
    Set<String> getPermissionList(Long id);

    /**
     * 根据用户id获取角色列表
     *
     * @param id 用户id
     * @return 角色列表
     */
    Set<String> getRoleList(Long id);
}
