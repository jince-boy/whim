package com.whim.system.model.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 租户套餐权限覆盖式分配参数。
 */
@Data
public class TenantPackagePermissionAssignDTO {
    @NotNull(message = "权限ID集合不能为空")
    private Set<@NotNull(message = "权限ID不能为空") Long> permissionIds;
}
