package com.whim.system.model.dto.dict;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 修改字典类型参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDictType.class, reverseConvertGenerate = false)
public class SysDictTypeUpdateDTO extends SysDictTypeAddDTO {
    /** 待修改记录的主键ID。 */
    @NotNull(message = "字典类型ID不能为空")
    @Positive(message = "字典类型ID必须大于0")
    private Long id;
}
