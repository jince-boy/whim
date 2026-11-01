package com.whim.system.model.dto.post;

import com.whim.system.model.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位修改参数，包含目标ID和可修改字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysPost.class, reverseConvertGenerate = false)
public class SysPostUpdateDTO extends SysPostSaveDTO {
    /** 待修改岗位的ID。 */
    @NotNull(message = "岗位ID不能为空")
    private Long id;
}
