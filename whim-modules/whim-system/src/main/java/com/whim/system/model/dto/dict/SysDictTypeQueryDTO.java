package com.whim.system.model.dto.dict;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 字典类型分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictTypeQueryDTO extends PageQueryDTO {
    /** 字典名称，精确查询。 */
    @Size(max = 64, message = "字典名称不能超过64个字符")
    private String name;
    /** 类型编码，精确查询。 */
    @Size(max = 128, message = "字典类型编码不能超过128个字符")
    private String type;
}
