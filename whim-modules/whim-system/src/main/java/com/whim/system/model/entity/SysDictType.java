package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID。
     */
    private Long id;
    /**
     * 字典名称。
     */
    private String name;
    /**
     * 字典类型编码。
     */
    private String type;
    /**
     * 备注，编辑时允许清空。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;
    /**
     * 删除标识：0未删除，已删除时为本行主键ID。
     */
    @TableLogic
    private Long deleted;
}
