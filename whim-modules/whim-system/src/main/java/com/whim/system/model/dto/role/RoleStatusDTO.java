package com.whim.system.model.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 租户角色状态修改参数。
 */
@Data
public class RoleStatusDTO {
    @NotNull(message = "角色状态不能为空")
    private Integer status;
}
