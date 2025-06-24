package com.whim.controller;

import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.LoginDTO;
import com.whim.system.model.vo.CaptchaVO;
import com.whim.system.model.vo.LoginVO;
import com.whim.system.service.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * date: 2024/10/23 21:46
 * description: 用户认证控制器
 */
@SystemApiPrefix
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final ISysUserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success("登录成功", userService.login(loginDTO));
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        return Result.success("验证码获取成功", userService.getCaptcha());
    }

    @GetMapping("/test")
    @SystemCheckPermission("adc")
//    @SaCheckPermission(type="system",value = "acsd")
    public Result<Void> test() {
        return Result.success("测试成功");
    }
}
