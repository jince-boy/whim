package com.whim.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * date: 2024/10/26 16:21
 * description: 登录数据传输对象
 */
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 18, message = "用户名长度在5~18位之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 18, message = "密码长度在8~18位之间")
    private String password;
    @NotBlank(message = "uuid不能为空")
    private String uuid;
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    @NotNull(message = "记住我不能为空")
    private Boolean rememberMe;
}
