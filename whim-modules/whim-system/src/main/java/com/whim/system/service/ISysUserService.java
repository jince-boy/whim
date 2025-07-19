package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysUser;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * @description 系统用户(SysUser)表服务接口
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return SysUser
     */
    SysUser getSysUserByUsername(String username);

    SysUser test();
}
