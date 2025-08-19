package com.whim.system.model.dto.sysDictType;

import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author jince
 * date: 2025/8/13 15:46
 * description: 字典类型DTO
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeQueryDTO {
    /**
     * 字典名称
     */
    private String name;
    /**
     * 字典类型(唯一标识)
     */
    private String type;
}
