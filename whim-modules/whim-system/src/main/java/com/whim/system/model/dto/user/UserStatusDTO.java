package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 全局用户状态修改参数。
 */
@Data
public class UserStatusDTO {
    @NotNull(message = "用户状态不能为空")
    private Integer status;
}
