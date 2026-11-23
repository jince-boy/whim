package com.whim.system.service;

import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.dto.auth.TwoFactorConfirmDTO;
import com.whim.system.model.dto.auth.TwoFactorDisableDTO;
import com.whim.system.model.dto.auth.TwoFactorLoginDTO;
import com.whim.system.model.dto.auth.TwoFactorSetupDTO;
import com.whim.system.model.dto.user.SysUserTwoFactorResetDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.model.vo.auth.LoginResultVO;
import com.whim.system.model.vo.auth.TwoFactorSetupVO;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 认证服务接口
 */
public interface IAuthService {
    /**
     * 获取当前用户信息并刷新当前令牌的授权快照。
     *
     * @return 当前登录用户信息
     */
    AuthUserVO getUserInfo();

    /**
     * 修改当前登录用户的个人资料，并返回更新后的资料与当前会话信息。
     *
     * @param parameter 允许本人维护的个人资料
     * @return 更新后的当前用户信息
     */
    AuthUserVO updateUserProfile(AuthProfileUpdateDTO parameter);

    /**
     * 校验原密码后修改本人密码，提交成功后使本人全部登录会话失效。
     *
     * @param parameter 原密码和新密码
     */
    void updateUserPassword(AuthPasswordUpdateDTO parameter);

    /**
     * 查询验证码开关，启用时签发与当前客户端地址绑定的 GIF 验证码。
     *
     * @param clientAddress 客户端地址
     * @return 验证码开关状态及已签发的挑战
     */
    LoginCaptchaVO createCaptcha(String clientAddress);

    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginResultVO login(AuthLoginDTO loginDTO);

    /**
     * 核验短时登录凭证及第二因素，恢复码仅用于解除丢失的验证器。
     *
     * @param parameter 密码挑战凭证和动态码或恢复码
     * @return 正式登录结果或恢复完成状态
     */
    LoginResultVO verifyLogin(TwoFactorLoginDTO parameter);

    /**
     * 查询当前登录用户是否已完成绑定。
     *
     * @return 是否已启用两步验证
     */
    boolean isTwoFactorEnabled();

    /**
     * 验证当前密码后准备密钥及一次性恢复码，尚未改变登录要求。
     *
     * @param parameter 本人当前密码
     * @return 仅显示一次的绑定资料
     */
    TwoFactorSetupVO startTwoFactorSetup(TwoFactorSetupDTO parameter);

    /**
     * 验证首次动态码，提交绑定后使旧会话失效。
     *
     * @param parameter 本次绑定凭证和首次动态码
     */
    void enableTwoFactor(TwoFactorConfirmDTO parameter);

    /**
     * 核验当前密码和第二因素，关闭本人的两步验证。
     *
     * @param parameter 当前密码及动态码或恢复码
     */
    void disableTwoFactor(TwoFactorDisableDTO parameter);

    /**
     * 核验超级管理员身份及本人密码，解除其他用户的指定验证器绑定。
     *
     * @param parameter 目标用户、预期绑定及操作者当前密码
     */
    void resetUserTwoFactor(SysUserTwoFactorResetDTO parameter);

    /**
     * 注销当前登录会话。
     */
    void logout();
}
