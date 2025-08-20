package com.whim.system.model.dto.sysDictData;

import com.whim.system.model.entity.SysDictData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author jince
 * date: 2025/8/19 23:48
 * description: 字典数据查询参数
 */
@Data
@AutoMapper(target = SysDictData.class)
public class SysDictDataQueryDTO {
    /**
     * 字典类型(关联sys_dict_type.type)
     */
    private String dictType;
    /**
     * 字典标签(显示文本)
     */
    private String label;
}
