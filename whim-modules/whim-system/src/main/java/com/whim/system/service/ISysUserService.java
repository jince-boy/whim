package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.system.model.entity.SysUser;

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
     * 构建系统账号认证上下文。
     *
     * @param user 用户实体
     * @return 用户认证信息
     */
    UserInfo buildUserInfo(SysUser user);
}

