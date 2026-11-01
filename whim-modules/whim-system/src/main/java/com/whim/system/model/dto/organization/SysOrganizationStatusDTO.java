package com.whim.system.model.dto.organization;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门和岗位的独立状态修改参数。
 */
@Data
public class SysOrganizationStatusDTO {
    /** 待修改部门或岗位的ID。 */
    @NotNull(message = "记录ID不能为空")
    private Long id;

    /** 状态：0正常、1停用。 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态无效")
    @Max(value = 1, message = "状态无效")
    private Integer status;
}
