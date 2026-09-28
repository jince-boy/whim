package com.whim.system.model.dto.permission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 全局权限状态修改参数。
 */
@Data
public class PermissionStatusDTO {
    @NotNull(message = "权限状态不能为空")
    private Integer status;
}
