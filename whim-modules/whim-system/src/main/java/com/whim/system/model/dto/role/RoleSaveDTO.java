package com.whim.system.model.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 系统角色保存参数。
 */
@Data
public class RoleSaveDTO {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 32, message = "角色名称不能超过32个字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 128, message = "角色编码不能超过128个字符")
    private String roleCode;
}
