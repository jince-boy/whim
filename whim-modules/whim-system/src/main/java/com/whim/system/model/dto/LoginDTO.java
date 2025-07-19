package com.whim.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2024/10/26 16:21
 * @description 登录数据传输对象
 */
@Data
public class LoginDTO {
    /**
     * 用户名，长度要求在5~18位之间，不能为空
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 18, message = "用户名长度必须在5~18位之间")
    private String username;

    /**
     * 密码，长度要求在8~18位之间，不能为空
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 18, message = "密码长度必须在8~18位之间")
    private String password;

    /**
     * 验证码对应的UUID，不能为空
     */
    @NotBlank(message = "UUID不能为空")
    private String uuid;

    /**
     * 验证码内容，不能为空
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 是否记住登录状态，不能为空
     */
    @NotNull(message = "记住我状态不能为空")
    private Boolean rememberMe;
}
