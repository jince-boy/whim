package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

import java.nio.charset.StandardCharsets;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 当前登录用户修改本人密码参数，不接收目标用户ID。
 */
@Data
public class AuthPasswordUpdateDTO {
    /** 原密码保留原输入，用于校验本人身份。 */
    @NotBlank(message = "原密码不能为空")
    @Size(max = 72, message = "原密码不能超过72个字符")
    @JsonDeserialize(using = StringDeserializer.class)
    @ToString.Exclude
    private String oldPassword;

    /** 新密码保留原输入，与用户管理使用相同的密码约束。 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 72, message = "新密码长度必须在8~72位之间")
    @Pattern(regexp = "(?U)\\S*", message = "新密码不能包含空白字符")
    @JsonDeserialize(using = StringDeserializer.class)
    @ToString.Exclude
    private String newPassword;

    /**
     * 校验原密码和新密码均不超过BCrypt支持的UTF-8字节长度。
     *
     * @return 两个密码的UTF-8编码是否均不超过72字节
     */
    @AssertTrue(message = "密码的UTF-8编码不能超过72字节")
    public boolean isPasswordLengthValid() {
        return (oldPassword == null || oldPassword.getBytes(StandardCharsets.UTF_8).length <= 72)
                && (newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72);
    }
}
