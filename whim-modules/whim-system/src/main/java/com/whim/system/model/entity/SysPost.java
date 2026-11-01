package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 系统岗位实体，岗位不授予角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 岗位ID。 */
    private Long id;
    /** 所属部门ID，null表示全组织岗位。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long deptId;
    /** 岗位名称。 */
    private String postName;
    /** 岗位编码。 */
    private String postCode;
    /** 排序值。 */
    private Integer sort;
    /** 状态：0正常、1停用。 */
    private Integer status;
    /** 备注。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;
    /** 删除标识：0未删除，已删除时为本行主键ID。 */
    @TableLogic
    private Long deleted;
}
