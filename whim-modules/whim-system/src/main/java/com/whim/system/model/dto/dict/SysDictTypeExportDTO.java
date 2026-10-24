package com.whim.system.model.dto.dict;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 字典类型导出条件，不接收列表分页参数。
 */
@Data
public class SysDictTypeExportDTO {
    /** 字典名称，精确查询。 */
    @Size(max = 64, message = "字典名称不能超过64个字符")
    private String name;
    /** 字典类型编码，精确查询。 */
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    private String type;
}
