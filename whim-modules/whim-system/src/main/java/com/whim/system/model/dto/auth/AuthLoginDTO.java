package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author jince
 * @date 2026/7/3
 * @description
 */
@Data
public class AuthLoginDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 18, message = "用户名长度必须在5~18位之间")
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 18, message = "密码长度必须在8~18位之间")
    private String password;
    /**
     * ALTCHA 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String altcha;
    /**
     * 记住我
     */
    private Boolean rememberMe;
}
