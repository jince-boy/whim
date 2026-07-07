package com.whim.system.service;

import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;

/**
 * @author jince
 * @date 2026/7/3
 * @description 认证服务接口
 */
public interface IAuthService {
    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    AuthLoginVO login(AuthLoginDTO loginDTO);

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    AltchaCaptchaVO getCaptcha();
}
