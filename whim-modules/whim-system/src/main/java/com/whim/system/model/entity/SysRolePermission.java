package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色权限关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 211867614786503794L;

    /**
     * id
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /** 操作级数据范围；为空时继承角色默认范围。 */
    private Integer dataScope;


}

