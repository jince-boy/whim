package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 校验当前密码和第二因素后关闭验证。
 */
@Getter
@Setter
public class TwoFactorDisableDTO {
    /** 本人当前密码，保留原始字符。 */
    @NotBlank(message = "请输入当前密码")
    @Size(max = 72, message = "密码长度不能超过72位")
    @JsonDeserialize(using = StringDeserializer.class)
    private String password;

    @NotBlank(message = "请输入动态码或恢复码")
    @Pattern(regexp = "(?:[0-9]{6}|[0-9a-fA-F]{32})", message = "请输入6位动态码或32位恢复码")
    private String code;
}
