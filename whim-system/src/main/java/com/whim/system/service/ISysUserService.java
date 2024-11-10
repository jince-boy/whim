package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.model.dto.LoginDTO;
import com.whim.model.entity.SysUser;
import com.whim.model.vo.CaptchaVO;
import com.whim.model.vo.LoginVO;

/**
 * @author Jince
 * date 2024-10-23 19:45:05
 * description: 系统用户(SysUser)表服务接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户登录
     *
     * @param loginDTO 用户登录数据传输对象
     * @return 用户登录响应实体类
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取验证码
     *
     * @return 验证码响应实体
     */
    CaptchaVO getCaptcha();

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return SysUser
     */
    SysUser getSysUserByUsername(String username);
}
