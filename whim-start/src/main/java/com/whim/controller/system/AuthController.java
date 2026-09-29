package com.whim.controller.system;

import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.utils.IPUtils;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.service.IAuthService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 认证控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/auth")
public class AuthController {

    /**
     * 认证服务对象
     */
    private final IAuthService authService;

    /**
     * 获取与客户端地址绑定的 GIF 验证码。
     *
     * @return 验证码 UUID、GIF 图片和有效期
     */
    @GetMapping("/captcha")
    public Result<LoginCaptchaVO> captcha() {
        LoginCaptchaVO captcha = authService.createCaptcha(IPUtils.getClientIpAddress());
        return Result.success("验证码获取成功", captcha);
    }

    /**
     * 用户登录并返回认证信息。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<AuthenticationToken> login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }

    /**
     * 注销当前登录会话。
     *
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("退出登录成功");
    }

    /**
     * 获取当前用户信息并按指定或默认租户刷新当前令牌上下文。
     *
     * @param tenantId 目标租户ID，不传时使用默认租户
     * @return 当前用户信息
     */
    @GetMapping("/userInfo")
    public Result<AuthUserVO> userInfo(@RequestParam(name = "tenantId", required = false) Long tenantId) {
        return Result.success("当前用户信息获取成功", authService.getUserInfo(tenantId));
    }
}
