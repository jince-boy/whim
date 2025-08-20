package com.whim.system.model.dto.sysDictType;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author jince
 * date: 2025/8/19 23:40
 * description: 字典类型添加DTO
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeInsertDTO {
    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String name;
    /**
     * 字典类型(唯一标识)
     */
    @NotBlank(message = "字典类型不能为空")
    private String type;
    /**
     * 备注
     */
    private String remark;
}
