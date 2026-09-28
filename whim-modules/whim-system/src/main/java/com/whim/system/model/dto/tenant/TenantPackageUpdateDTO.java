package com.whim.system.model.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 平台调整租户套餐的参数。
 */
@Data
public class TenantPackageUpdateDTO {
    @NotNull(message = "套餐ID不能为空")
    private Long packageId;
}
