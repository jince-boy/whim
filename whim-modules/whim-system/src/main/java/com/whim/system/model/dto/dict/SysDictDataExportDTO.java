package com.whim.system.model.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 指定字典类型的数据导出条件，不接收列表分页参数。
 */
@Data
public class SysDictDataExportDTO {
    /** 必须指定所属类型，防止误导出其他字典的数据。 */
    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "字典类型编码格式错误")
    private String type;
    /** 字典标签，精确查询。 */
    @Size(max = 128, message = "字典标签不能超过128个字符")
    private String label;
}
