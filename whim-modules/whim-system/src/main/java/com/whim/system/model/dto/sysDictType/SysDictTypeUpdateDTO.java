package com.whim.system.model.dto.sysDictType;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author jince
 * date: 2025/8/19 23:42
 * description: 字典类型更新DTO
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeUpdateDTO {
    /**
     * 字典ID
     */
    @NotNull(message = "字典ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
