package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 超级管理员解除其他用户的两步验证绑定。
 */
@Getter
@Setter
public class SysUserTwoFactorResetDTO {
    /** 需要找回账号的用户ID。 */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 打开重置弹窗时的绑定标识，防止旧请求重置新的验证器。 */
    @NotNull(message = "绑定标识不能为空")
    private Long bindingId;

    /** 操作者本人的当前密码，按原文验证。 */
    @NotBlank(message = "请输入超级管理员的当前密码")
    @Size(max = 72, message = "密码长度不能超过72位")
    @JsonDeserialize(using = StringDeserializer.class)
    private String currentPassword;
}
