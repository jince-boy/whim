package com.whim.satoken.service;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/01
 * @description 认证授权查询扩展点，由具体业务模块实现角色与权限数据查询。
 */
public interface IAuthQueryService {
    /**
     * 判断当前查询服务是否支持指定账号体系。
     *
     * @param loginType Sa-Token 账号体系标识
     * @return true 表示支持该账号体系
     */
    default boolean supports(String loginType) {
        return true;
    }

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
