package com.whim.system.service;

import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;
import com.whim.system.model.vo.auth.AuthUserVO;

/**
 * @author Jince
 * @date 2026/07/03
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
     * 注销当前登录会话。
     */
    void logout();

    /**
     * 获取当前用户信息并刷新当前令牌的租户上下文。
     *
     * @param tenantId 目标租户ID，不传时使用默认租户
     * @return 当前登录用户信息
     */
    AuthUserVO getUserInfo(Long tenantId);

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    AltchaCaptchaVO getCaptcha();
}
