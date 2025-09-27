package com.whim.system.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/8/13 16:17
 * description: 字典数据VO
 */
@Data
@AutoMapper(target = SysDictData.class)
public class SysDictDataVO {
    /**
     * 字典数据ID
     */
    @ExcelProperty("字典数据id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 字典类型(关联sys_dict_type.type)
     */
    @ExcelProperty("字典类型")
    private String dictType;
    /**
     * 字典标签(显示文本)
     */
    @ExcelProperty("字典标签")
    private String label;
    /**
     * 字典键值(存储值)
     */
    @ExcelProperty("字典键值")
    private String value;
    /**
     * 回显样式类名(如: primary)
     */
    @ExcelProperty("回显样式类名")
    private String listClass;
    /**
     * 排序(升序)
     */
    @ExcelIgnore
    private Integer sort;
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
