package com.whim.controller.system;

import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.system.model.dto.auth.LoginDTO;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;
import com.whim.system.service.ISysAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2024/10/23 21:46
 * @description 用户认证
 */
@SystemApiPrefix
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ISysAuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        return Result.success("验证码获取成功", authService.getCaptcha());
    }
}
