package com.whim.system.model.dto.dept;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 租户部门启停参数。
 */
@Data
public class DeptStatusDTO {
    @NotNull(message = "部门状态不能为空")
    private Integer status;
}
