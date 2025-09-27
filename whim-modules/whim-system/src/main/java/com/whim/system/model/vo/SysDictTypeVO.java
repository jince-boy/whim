package com.whim.system.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.whim.system.model.entity.SysDictType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jince
 * @date 2025/6/27 19:31
 * @description 字典类型VO
 */
@Data
@AutoMapper(target = SysDictType.class)
public class SysDictTypeVO {
    /**
     * 字典ID
     */
    @ExcelProperty("字典类型ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 字典名称
     */
    @ExcelProperty("字典名称")
    private String name;
    /**
     * 字典类型(唯一标识)
     */
    @ExcelProperty("字典类型")
    private String type;
    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
