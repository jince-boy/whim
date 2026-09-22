package com.whim.system.model.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 切换当前租户请求参数。
 */
@Data
public class SwitchTenantDTO {

    /**
     * 目标租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;
}
