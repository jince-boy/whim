package com.whim.system.model.dto.dept;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 部门树查询参数。
 */
@Data
public class SysDeptQueryDTO {
    /** 部门名称，模糊匹配；为空时不筛选名称。 */
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    /** 部门状态：0正常、1停用；为空时不筛选状态。 */
    @Min(value = 0, message = "部门状态无效")
    @Max(value = 1, message = "部门状态无效")
    private Integer status;
}
