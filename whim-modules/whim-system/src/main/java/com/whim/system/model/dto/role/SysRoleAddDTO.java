package com.whim.system.model.dto.role;

import com.whim.system.model.entity.SysRole;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色新增参数，初始数据范围由服务端设为本人。
 */
@Data
@AutoMapper(target = SysRole.class, reverseConvertGenerate = false)
public class SysRoleAddDTO {
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 32, message = "角色名称不能超过32个字符")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 128, message = "角色编码不能超过128个字符")
    private String roleCode;
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能小于0")
    private Integer sort = 0;
    @NotNull(message = "角色状态不能为空")
    @Min(value = 0, message = "角色状态无效")
    @Max(value = 1, message = "角色状态无效")
    private Integer status = 0;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
