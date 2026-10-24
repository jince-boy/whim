package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 499195426849018744L;

    /**
     * id
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色统一数据范围(1-全部 2-自定义部门 3-本部门 4-本部门及下级 5-本人)
     */
    private Integer dataScope;

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
    private String remark;

    /**
     * 删除标识：0表示未删除，已删除时为本行主键ID
     */
    @TableLogic
    private Long deleted;

}

