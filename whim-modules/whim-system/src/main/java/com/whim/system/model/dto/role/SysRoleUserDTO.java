package com.whim.system.model.dto.role;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/15
 * @description 给角色批量分配或移除用户，不修改用户的其他角色。
 */
@Data
public class SysRoleUserDTO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    @NotEmpty(message = "请选择用户")
    @Size(max = 100, message = "每次最多操作100个用户")
    private List<@NotNull(message = "用户ID不能为空") Long> userIds;
}
