package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 凭首次动态码确认本次绑定。
 */
@Getter
@Setter
public class TwoFactorConfirmDTO {
    @NotBlank(message = "缺少验证凭证")
    @Pattern(regexp = "[0-9a-f]{32}", message = "验证凭证格式错误")
    private String setupId;

    @NotBlank(message = "请输入动态码")
    @Pattern(regexp = "[0-9]{6}", message = "请输入6位动态码")
    private String code;
}
