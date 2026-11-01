package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统部门表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDept extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -63138490628218808L;

    /**
     * id
     */
    private Long id;

    /**
     * 父级部门ID
     */
    private Long parentId;

    /**
     * 祖级部门ID集合
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码，未删除部门之间唯一
     */
    private String deptCode;

    /**
     * 负责人用户ID
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long leaderUserId;

    /**
     * 联系电话
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String phone;

    /**
     * 邮箱
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String email;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0-正常 1-停用)
     */
    private Integer status;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;

    /**
     * 删除标识：0表示未删除，已删除时为本行主键ID
     */
    @TableLogic
    private Long deleted;

}

