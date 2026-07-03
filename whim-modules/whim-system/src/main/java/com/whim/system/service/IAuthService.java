package com.whim.system.service;

import com.whim.system.model.dto.AltchaCaptchaDTO;

/**
 * @author jince
 * @date 2026/7/3
 * @description 认证服务接口
 */
public interface IAuthService {

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    AltchaCaptchaDTO getCaptcha();
}
