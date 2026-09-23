package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.system.model.entity.SysUser;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户表服务接口
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 根据用户名查询未删除用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getByUsername(String username);

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 可访问租户ID集合
     */
    Set<Long> getAccessibleTenantIds(Long userId);

    /**
     * 构建系统账号认证上下文。
     *
     * @param user 用户实体
     * @return 用户认证信息
     */
    UserInfo buildUserInfo(SysUser user);

    /**
     * 按指定当前租户构建系统账号认证上下文。
     *
     * @param user            用户实体
     * @param currentTenantId 当前租户ID，平台上下文时为空
     * @return 用户认证信息
     */
    UserInfo buildUserInfo(SysUser user, Long currentTenantId);

}

