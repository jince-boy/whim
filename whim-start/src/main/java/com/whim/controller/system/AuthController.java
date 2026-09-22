package com.whim.controller.system;

import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.service.IAuthService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 用户登录并返回认证信息。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @CrossOrigin
    @PostMapping("/login")
    public Result<AuthLoginVO> login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }

    /**
     * 注销当前登录会话。
     *
     * @return 注销结果
     */
    @CrossOrigin
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("退出登录成功");
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @CrossOrigin
    @GetMapping("/userInfo")
    public Result<AuthUserVO> userInfo() {
        return Result.success("当前用户信息获取成功", authService.getUserInfo());
    }

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    @CrossOrigin
    @GetMapping("/captcha")
    public Result<AltchaCaptchaVO> getCaptcha() {
        return Result.success("验证码获取成功", authService.getCaptcha());
    }
}
