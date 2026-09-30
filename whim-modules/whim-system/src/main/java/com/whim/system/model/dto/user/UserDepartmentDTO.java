package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统用户主部门分配参数。
 */
@Data
public class UserDepartmentDTO {
    @NotNull(message = "主部门不能为空")
    @Positive(message = "部门ID必须为正数")
    private Long deptId;
}
