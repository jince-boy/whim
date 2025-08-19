package com.whim.system.service;

import com.whim.system.model.dto.auth.LoginDTO;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;

/**
 * @author jince
 * @date 2025/6/26 15:15
 * @description 认证服务
 */
public interface ISysAuthService {
    /**
     * 用户登录
     *
     * @param loginDTO 用户登录数据传输对象
     * @return 用户登录响应实体类
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 获取验证码
     *
     * @return 验证码响应实体
     */
    CaptchaVO getCaptcha();
}
