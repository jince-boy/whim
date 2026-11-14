package com.whim.controller.system;

import com.whim.core.annotation.RequestDecrypt;
import com.whim.core.auth.model.AuthenticationToken;
import com.whim.core.utils.IPUtils;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysUserAvatarService;
import com.whim.web.annotation.XssIgnore;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /** 当前账号头像上传服务。 */
    private final ISysUserAvatarService userAvatarService;

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
     * 解密并校验登录参数后返回认证信息，密码保持原始字符。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @RequestDecrypt
    @XssIgnore
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
     * 获取当前用户资料和当前有效授权。
     *
     * @return 当前用户信息
     */
    @GetMapping("/userInfo")
    public Result<AuthUserVO> userInfo() {
        return Result.success("当前用户信息获取成功", authService.getUserInfo());
    }

    /**
     * 保存当前登录用户的个人资料，不允许指定其他用户或修改组织授权。
     *
     * @param parameter 本人个人资料
     * @return 更新后的当前用户信息
     */
    @SystemCheckLogin
    @PutMapping("/profile")
    public Result<AuthUserVO> updateUserProfile(@RequestBody @Valid AuthProfileUpdateDTO parameter) {
        return Result.success("个人资料保存成功", authService.updateUserProfile(parameter));
    }

    /**
     * 解密原密码和新密码并修改本人密码，成功后要求重新登录。
     *
     * @param parameter 原密码和新密码
     * @return 修改密码结果
     */
    @SystemCheckLogin
    @RequestDecrypt
    @XssIgnore
    @PutMapping("/password")
    public Result<Void> updateUserPassword(@RequestBody @Valid AuthPasswordUpdateDTO parameter) {
        authService.updateUserPassword(parameter);
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 为当前登录用户上传头像，由个人资料保存接口建立头像关联。
     *
     * @param parameter 本地头像图片
     * @return 头像地址
     */
    @SystemCheckLogin
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> addUserAvatar(@Valid @ModelAttribute SysUserAvatarUploadDTO parameter) {
        return Result.success("头像上传成功", userAvatarService.addUserAvatar(parameter));
    }
}
