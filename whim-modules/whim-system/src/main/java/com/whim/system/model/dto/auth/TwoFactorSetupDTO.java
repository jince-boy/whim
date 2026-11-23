package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 申请绑定当前账号的验证器。
 */
@Getter
@Setter
public class TwoFactorSetupDTO {
    /** 本人当前密码，保留原始字符。 */
    @NotBlank(message = "请输入当前密码")
    @Size(max = 72, message = "密码长度不能超过72位")
    @JsonDeserialize(using = StringDeserializer.class)
    private String password;
}
