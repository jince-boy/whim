package com.whim.system.service;

import com.whim.core.auth.model.AuthenticationToken;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;

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
     * 生成与当前客户端地址绑定的登录 GIF 验证码。
     *
     * @param clientAddress 客户端地址
     * @return GIF 验证码
     */
    LoginCaptchaVO createCaptcha(String clientAddress);

    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    AuthenticationToken login(AuthLoginDTO loginDTO);

    /**
     * 注销当前登录会话。
     */
    void logout();
}
