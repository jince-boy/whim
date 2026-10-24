package com.whim.system.model.dto.dict;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 新增字典类型参数，不接受主键、审计字段和删除标识。
 */
@Data
@AutoMapper(target = SysDictType.class, reverseConvertGenerate = false)
public class SysDictTypeAddDTO {
    /**
     * 字典名称。
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称不能超过64个字符")
    private String name;
    /**
     * 字典类型编码。
     */
    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "字典类型编码须以字母开头且只能包含字母、数字和下划线")
    private String type;
    /**
     * 备注。
     */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
