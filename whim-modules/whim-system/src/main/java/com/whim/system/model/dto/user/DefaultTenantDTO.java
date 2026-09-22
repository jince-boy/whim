package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 设置默认租户请求参数。
 */
@Data
public class DefaultTenantDTO {

    /**
     * 默认租户ID
     */
    @NotNull(message = "默认租户ID不能为空")
    private Long tenantId;
}
