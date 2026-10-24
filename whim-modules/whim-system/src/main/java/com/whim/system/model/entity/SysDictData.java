package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典数据，使用类型编码关联字典类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictData extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 所属字典类型。
     */
    private String type;
    /**
     * 字典标签。
     */
    private String label;
    /**
     * 字典值。
     */
    private String value;
    /**
     * 回显样式。
     */
    private String listClass;
    /**
     * 排序值。
     */
    private Integer sort;
    /**
     * 备注，编辑时允许清空。
     */
    private String remark;
    /**
     * 删除标识：0未删除，已删除时为本行主键ID。
     */
    @TableLogic
    private Long deleted;
}
