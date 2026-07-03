package com.whim.system.controller;

import com.whim.core.web.Result;
import com.whim.system.model.dto.AltchaCaptchaDTO;
import com.whim.system.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/7/3
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
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    @CrossOrigin
    @GetMapping("/captcha")
    public Result<AltchaCaptchaDTO> getCaptcha() {
        return Result.success("验证码获取成功", authService.getCaptcha());
    }
}
