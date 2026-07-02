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
     * 角色类型(0-系统 1-租户)
     */
    private Integer roleType;

    /**
     * 数据范围(1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限)
     */
    private Integer dataScope;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 状态(0-正常 1-停用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer deleted;

}

