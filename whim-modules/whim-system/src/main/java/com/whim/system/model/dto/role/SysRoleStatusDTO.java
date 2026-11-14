package com.whim.system.model.dto.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色状态变更参数。
 */
@Data
public class SysRoleStatusDTO {
    @NotNull(message = "角色ID不能为空")
    private Long id;
    @NotNull(message = "角色状态不能为空")
    @Min(value = 0, message = "角色状态无效")
    @Max(value = 1, message = "角色状态无效")
    private Integer status;
}
