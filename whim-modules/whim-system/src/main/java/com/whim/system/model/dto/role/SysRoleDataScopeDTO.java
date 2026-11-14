package com.whim.system.model.dto.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色统一数据范围及完整自定义部门集合。
 */
@Data
public class SysRoleDataScopeDTO {
    @NotNull(message = "角色ID不能为空")
    private Long id;
    @NotNull(message = "数据范围不能为空")
    @Min(value = 1, message = "数据范围无效")
    @Max(value = 5, message = "数据范围无效")
    private Integer dataScope;
    @NotNull(message = "自定义部门集合不能为空")
    @Size(max = 1000, message = "最多选择1000个自定义部门")
    private List<@NotNull(message = "部门ID不能为空") Long> deptIds;
}
