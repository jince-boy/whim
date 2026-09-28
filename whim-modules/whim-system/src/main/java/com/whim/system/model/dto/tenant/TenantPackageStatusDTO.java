package com.whim.system.model.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 租户套餐状态修改参数。
 */
@Data
public class TenantPackageStatusDTO {
    @NotNull(message = "套餐状态不能为空")
    private Integer status;
}
