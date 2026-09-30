package com.whim.system.model.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 系统角色数据范围配置参数。
 */
@Data
public class RoleDataScopeDTO {
    @NotNull(message = "数据范围不能为空")
    private Integer dataScope;

    @NotNull(message = "部门ID集合不能为空")
    private Set<@NotNull(message = "部门ID不能为空") Long> deptIds;
}
