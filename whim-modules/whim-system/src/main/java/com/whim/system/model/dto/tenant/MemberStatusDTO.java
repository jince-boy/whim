package com.whim.system.model.dto.tenant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 租户成员状态修改参数。
 */
@Data
public class MemberStatusDTO {
    @NotNull(message = "成员状态不能为空")
    private Integer status;
}
