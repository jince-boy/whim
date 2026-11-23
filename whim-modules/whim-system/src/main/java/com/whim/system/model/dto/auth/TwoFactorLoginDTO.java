package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 完成密码校验后的第二步登录或账号找回。
 */
@Getter
@Setter
public class TwoFactorLoginDTO {
    @NotBlank(message = "缺少验证凭证")
    @Pattern(regexp = "[0-9a-f]{32}", message = "验证凭证格式错误")
    private String challengeId;

    @NotBlank(message = "请输入动态码或恢复码")
    @Pattern(regexp = "(?:[0-9]{6}|[0-9a-fA-F]{32})", message = "请输入6位动态码或32位恢复码")
    private String code;
}
