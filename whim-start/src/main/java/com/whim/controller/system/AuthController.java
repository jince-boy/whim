package com.whim.controller.system;

import com.whim.core.annotation.RequestDecrypt;
import com.whim.core.crypto.RequestEncryptionCipher;
import com.whim.core.utils.IPUtils;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.dto.auth.AuthPasswordUpdateDTO;
import com.whim.system.model.dto.auth.AuthProfileUpdateDTO;
import com.whim.system.model.dto.auth.TwoFactorConfirmDTO;
import com.whim.system.model.dto.auth.TwoFactorDisableDTO;
import com.whim.system.model.dto.auth.TwoFactorLoginDTO;
import com.whim.system.model.dto.auth.TwoFactorSetupDTO;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.vo.auth.AuthUserVO;
import com.whim.system.model.vo.auth.LoginCaptchaVO;
import com.whim.system.model.vo.auth.LoginResultVO;
import com.whim.system.model.vo.auth.TwoFactorSetupVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysUserService;
import com.whim.web.annotation.XssIgnore;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    private final ISysUserService userService;

    private final RequestEncryptionCipher requestEncryptionCipher;

    /**
     * 获取当前请求加密公钥，供未登录客户端自动初始化加密能力。
     *
     * @param response HTTP 响应
     * @return SPKI DER 单行 Base64 公钥
     */
    @GetMapping("/requestEncryptionKey")
    public Result<String> getRequestEncryptionKey(HttpServletResponse response) {
        // 公钥轮换后必须重新获取，避免浏览器或代理持久缓存旧公钥。
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success("请求加密公钥获取成功", requestEncryptionCipher.getPublicKey());
    }

    /**
     * 查询登录验证码开关，启用时获取与客户端地址绑定的 GIF 验证码。
     *
     * @return 验证码开关状态及已签发的挑战
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
    public Result<LoginResultVO> login(@RequestBody @Valid AuthLoginDTO loginDTO) {
        LoginResultVO result = authService.login(loginDTO);
        return Result.success(result.getChallengeId() == null ? "登录成功" : "请完成两步验证", result);
    }

    /**
     * 核验短时登录凭证及第二因素，恢复码仅用于解除丢失的验证器。
     *
     * @param parameter 密码挑战凭证和动态码或恢复码
     * @return 正式登录结果或恢复完成状态
     */
    @PostMapping("/verifyLogin")
    @RequestDecrypt
    public Result<LoginResultVO> verifyLogin(@RequestBody @Valid TwoFactorLoginDTO parameter) {
        LoginResultVO result = authService.verifyLogin(parameter);
        return Result.success(Boolean.TRUE.equals(result.getRecovered()) ? "已解除旧绑定，请重新登录" : "登录成功", result);
    }

    /**
     * 获取本人的两步验证状态。
     *
     * @return 是否已开启
     */
    @SystemCheckLogin
    @GetMapping("/twoFactorStatus")
    public Result<Boolean> getTwoFactorStatus() {
        return Result.success("查询成功", authService.isTwoFactorEnabled());
    }

    /**
     * 校验本人密码并准备验证器绑定资料。
     *
     * @param parameter 当前密码
     * @param response 禁止缓存含密钥的响应
     * @return 待确认的绑定资料
     */
    @SystemCheckLogin
    @RequestDecrypt
    @XssIgnore
    @PostMapping("/twoFactorSetup")
    public Result<TwoFactorSetupVO> startTwoFactorSetup(@RequestBody @Valid TwoFactorSetupDTO parameter, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success("请绑定验证器", authService.startTwoFactorSetup(parameter));
    }

    /**
     * 验证首次动态码并开启两步验证。
     *
     * @param parameter 绑定凭证和首次动态码
     * @return 绑定结果
     */
    @SystemCheckLogin
    @RequestDecrypt
    @Log(title = "开启两步验证", type = LogType.UPDATE, excludeFields = {"setupId", "code"})
    @PostMapping("/twoFactorEnable")
    public Result<Void> enableTwoFactor(@RequestBody @Valid TwoFactorConfirmDTO parameter) {
        authService.enableTwoFactor(parameter);
        return Result.success("两步验证已开启，请使用下一枚动态码重新登录");
    }

    /**
     * 校验本人密码和第二因素后关闭两步验证。
     *
     * @param parameter 当前密码及动态码或恢复码
     * @return 解除绑定结果
     */
    @SystemCheckLogin
    @RequestDecrypt
    @XssIgnore
    @Log(title = "关闭两步验证", type = LogType.UPDATE, excludeFields = {"password", "code"})
    @PostMapping("/twoFactorDisable")
    public Result<Void> disableTwoFactor(@RequestBody @Valid TwoFactorDisableDTO parameter) {
        authService.disableTwoFactor(parameter);
        return Result.success("两步验证已关闭，请重新登录");
    }

    /**
     * 注销当前登录会话。
     *
     * @return 注销结果
     */
    @Log(title = "账户退出", type = LogType.OTHER)
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
    @Log(title = "个人中心", type = LogType.UPDATE)
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
    @Log(title = "修改本人密码", type = LogType.UPDATE, excludeFields = {"oldPassword", "newPassword"})
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
    @Log(title = "上传头像", type = LogType.UPDATE, excludeFields = "file")
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> addUserAvatar(@Valid @ModelAttribute SysUserAvatarUploadDTO parameter) {
        return Result.success("头像上传成功", userService.addUserAvatar(parameter));
    }
}
