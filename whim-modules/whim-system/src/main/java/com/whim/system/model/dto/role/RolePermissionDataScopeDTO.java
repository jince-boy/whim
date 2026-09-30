package com.whim.system.model.dto.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 操作级范围覆盖，dataScope为空时恢复继承角色默认范围。
 */
@Data
public class RolePermissionDataScopeDTO {
    @Min(value = 1, message = "数据范围只能为1至5")
    @Max(value = 5, message = "数据范围只能为1至5")
    private Integer dataScope;
    @NotNull(message = "部门ID集合不能为空")
    private Set<@NotNull(message = "部门ID不能为空") Long> deptIds;
}
