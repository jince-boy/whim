package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.system.model.entity.SysUser;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号查询及认证信息构建。
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    SysUser getUserById(Long id);

    /**
     * 登录阶段按规范化用户名查询账号。
     */
    SysUser getByUsername(String username);

    /**
     * 构建登录账号的角色和精确权限快照。
     */
    UserInfo buildUserInfo(SysUser user);
}
