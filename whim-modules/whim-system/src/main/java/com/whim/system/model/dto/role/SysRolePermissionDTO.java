package com.whim.system.model.dto.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色权限分配参数，仅保存明确勾选的菜单与按钮。
 */
@Data
public class SysRolePermissionDTO {
    @NotNull(message = "角色ID不能为空")
    private Long id;
    @NotNull(message = "菜单权限集合不能为空")
    @Size(max = 5000, message = "最多选择5000项菜单权限")
    private List<@NotNull(message = "菜单权限ID不能为空") Long> permissionIds;
}
