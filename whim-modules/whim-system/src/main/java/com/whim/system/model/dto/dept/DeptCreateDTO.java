package com.whim.system.model.dto.dept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 组织部门创建参数，父部门ID为0表示根部门。
 */
@Data
public class DeptCreateDTO {
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过64个字符")
    private String deptName;

    @NotNull(message = "排序不能为空")
    private Integer sort;
}
