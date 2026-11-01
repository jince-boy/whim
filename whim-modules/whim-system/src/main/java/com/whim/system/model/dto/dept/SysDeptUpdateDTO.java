package com.whim.system.model.dto.dept;

import com.whim.system.model.entity.SysDept;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门修改参数，包含目标ID和可修改字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDept.class, reverseConvertGenerate = false)
public class SysDeptUpdateDTO extends SysDeptSaveDTO {
    /** 待修改部门的ID。 */
    @NotNull(message = "部门ID不能为空")
    private Long id;
}
