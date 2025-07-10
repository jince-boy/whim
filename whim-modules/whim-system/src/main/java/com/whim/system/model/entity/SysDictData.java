package com.whim.system.model.entity;

import com.whim.mybatis.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据表(SysDictData)实体类
 *
 * @author Jince
 * @since 2025-06-27 17:23:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDictData extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 752011170287041150L;
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
     * 内联样式(如: color:red;)
     */
    private String cssStyle;
    /**
     * 回显样式类名(如: primary)
     */
    private String listClass;
    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 是否默认(0-否 1-是)
     */
    private Integer isDefault;
    /**
     * 状态(0-启用 1-禁用)
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
}
