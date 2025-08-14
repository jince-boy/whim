package com.whim.system.model.vo;

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
    private Long id;
    /**
     * 字典类型(关联sys_dict_type.type)
     */
    private String dictType;
    /**
     * 字典标签(显示文本)
     */
    private String label;
    /**
     * 字典键值(存储值)
     */
    private String value;
    /**
     * 回显样式类名(如: primary)
     */
    private String listClass;
    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
