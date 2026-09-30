package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/07/03
 * @description 登录请求参数
 */
@Data
public class AuthLoginDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在3~32位之间")
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须在8~72位之间")
    private String password;

    /**
     * 验证码 UUID。
     */
    @NotBlank(message = "验证码标识不能为空")
    @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            message = "验证码标识格式错误")
    private String uuid;

    /**
     * GIF 图片中的四位字符。
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "[A-Za-z0-9]{4}", message = "验证码格式错误")
    private String captchaCode;

    /**
     * 记住我
     */
    private Boolean rememberMe;
}
