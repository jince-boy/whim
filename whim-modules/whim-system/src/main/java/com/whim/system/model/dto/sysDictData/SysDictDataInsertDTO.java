package com.whim.system.model.dto.sysDictData;

import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author jince
 * date: 2025/8/19 23:48
 * description: 字典数据新增DTO
 */
@Data
@AutoMapper(target = SysDictData.class)
public class SysDictDataInsertDTO {
    /**
     * 字典类型(关联sys_dict_type.type)
     */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    /**
     * 字典标签(显示文本)
     */
    @NotBlank(message = "字典标签不能为空")
    private String label;
    /**
     * 字典键值(存储值)
     */
    @NotBlank(message = "字典键值不能为空")
    private String value;
    /**
     * 回显样式类名(如: primary)
     */
    @NotBlank(message = "回显样式不能为空")
    private String listClass;
    /**
     * 排序(升序)
     */
    @NotNull(message = "字典排序不能为空")
    private Integer sort;
    /**
     * 备注
     */
    private String remark;
}
