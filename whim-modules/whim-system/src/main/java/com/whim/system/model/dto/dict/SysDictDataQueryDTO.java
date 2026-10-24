package com.whim.system.model.dto.dict;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 指定字典类型的数据分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictDataQueryDTO extends PageQueryDTO {
    /** 字典类型编码，精确查询。 */
    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "字典类型编码格式错误")
    private String type;
    /** 字典标签，精确查询。 */
    @Size(max = 128, message = "字典标签不能超过128个字符")
    private String label;
}
