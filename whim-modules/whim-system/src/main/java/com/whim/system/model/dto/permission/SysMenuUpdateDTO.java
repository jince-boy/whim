package com.whim.system.model.dto.permission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 菜单修改参数，目标ID和全部可修改字段统一放入请求对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysPermission.class, reverseConvertGenerate = false)
public class SysMenuUpdateDTO extends SysMenuSaveDTO {
    /** 目标菜单ID。 */
    @NotNull(message = "菜单ID不能为空")
    private Long id;
}
