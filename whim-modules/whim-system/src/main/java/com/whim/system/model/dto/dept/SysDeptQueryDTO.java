package com.whim.system.model.dto.dept;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门树查询参数，匹配时保留可见的祖先节点。
 */
@Data
public class SysDeptQueryDTO {
    /** 部门名称，模糊匹配。 */
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    /** 状态：0正常、1停用。 */
    @Min(value = 0, message = "部门状态无效")
    @Max(value = 1, message = "部门状态无效")
    private Integer status;

    /** 排除的部门及其下级，用于选择新上级。 */
    @Positive(message = "排除部门ID必须大于0")
    private Long excludeDeptId;
}
