package com.whim.system.service;

import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;
import com.whim.system.model.vo.auth.AuthTenantListVO;
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
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    AuthUserVO getUserInfo();

    /**
     * 获取当前登录用户可选择的租户列表。
     *
     * @return 租户选择列表
     */
    AuthTenantListVO getTenantList();

    /**
     * 切换当前令牌正在操作的租户。
     *
     * @param tenantId 目标租户ID
     * @return 切换后的当前用户信息
     */
    AuthUserVO switchTenant(Long tenantId);

    /**
     * 将超级管理员切换回平台上下文。
     *
     * @return 切换后的当前用户信息
     */
    AuthUserVO switchPlatform();

    /**
     * 设置当前用户默认进入的租户。
     *
     * @param tenantId 默认租户ID
     * @return 更新后的当前用户信息
     */
    AuthUserVO setDefaultTenant(Long tenantId);

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    AltchaCaptchaVO getCaptcha();
}
