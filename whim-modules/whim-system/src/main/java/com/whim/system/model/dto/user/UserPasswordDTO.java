package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 管理员重置目标账号密码参数。
 */
@Data
public class UserPasswordDTO {
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须为8至72个字符")
    private String password;
}
