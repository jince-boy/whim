package com.whim.satoken.service;

import java.util.Set;

/**
 * @author jince
 * @date 2026/4/9
 * @description 权限查询服务
 */
public interface IAuthQueryService {
    /**
     * 获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> getPermissionList(Long userId);

    /**
     * 获取角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    Set<String> getRoleList(Long userId);
}
