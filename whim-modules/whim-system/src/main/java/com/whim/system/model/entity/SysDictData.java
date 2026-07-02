package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 字典数据表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictData extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 546551085386319176L;

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

}

