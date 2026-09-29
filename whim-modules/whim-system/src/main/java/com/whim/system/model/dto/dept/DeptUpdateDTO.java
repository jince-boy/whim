package com.whim.system.model.dto.dept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 租户部门基础信息修改参数，不允许隐式移动部门树。
 */
@Data
public class DeptUpdateDTO {
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    @NotNull(message = "排序不能为空")
    private Integer sort;
}
