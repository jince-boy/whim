package com.whim.system.model.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

import java.nio.charset.StandardCharsets;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 管理员修改用户密码参数。
 */
@Data
public class SysUserPasswordDTO {
    /** 用户ID。 */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 密码保留原输入，不允许包含空白字符。 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须在8~72位之间")
    @Pattern(regexp = "(?U)\\S*", message = "密码不能包含空白字符")
    @JsonDeserialize(using = StringDeserializer.class)
    @ToString.Exclude
    private String password;

    /**
     * 校验 BCrypt 支持的密码字节长度。
     *
     * @return 密码的UTF-8编码是否不超过72字节
     */
    @AssertTrue(message = "密码的UTF-8编码不能超过72字节")
    public boolean isPasswordLengthValid() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
