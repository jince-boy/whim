package com.whim.system.model.dto.role;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色分页筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQueryDTO extends PageQueryDTO {
    @Size(max = 32, message = "角色名称不能超过32个字符")
    private String roleName;
    @Size(max = 128, message = "角色编码不能超过128个字符")
    private String roleCode;
    @Min(value = 0, message = "角色状态无效")
    @Max(value = 1, message = "角色状态无效")
    private Integer status;
}
