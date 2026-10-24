package com.whim.system.model.dto.dict;

import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 修改字典数据参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysDictData.class, reverseConvertGenerate = false)
public class SysDictDataUpdateDTO extends SysDictDataAddDTO {
    /** 待修改记录的主键ID。 */
    @NotNull(message = "字典数据ID不能为空")
    @Positive(message = "字典数据ID必须大于0")
    private Long id;
}
