package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 角色-权限关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -97642885482502548L;

    /**
     * 关联ID
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

}

