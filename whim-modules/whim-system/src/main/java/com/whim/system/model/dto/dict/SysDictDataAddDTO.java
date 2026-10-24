package com.whim.system.model.dto.dict;

import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 新增字典数据参数。
 */
@Data
@AutoMapper(target = SysDictData.class, reverseConvertGenerate = false)
public class SysDictDataAddDTO {
    /** 所属字典类型编码。 */
    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "字典类型编码格式错误")
    private String type;
    /** 字典标签。 */
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签不能超过128个字符")
    private String label;
    /** 字典值，以字符串保留前导零等业务含义。 */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 128, message = "字典值不能超过128个字符")
    private String value;
    /** 回显样式。 */
    @NotBlank(message = "回显样式不能为空")
    @Pattern(regexp = "default|primary|info|success|warning|error", message = "字典回显样式无效")
    private String listClass = "default";
    /** 排序值，按升序展示。 */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort = 0;
    /** 备注。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
