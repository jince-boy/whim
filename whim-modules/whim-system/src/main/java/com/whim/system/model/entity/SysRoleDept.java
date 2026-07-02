package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 角色与部门关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleDept extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -91847725850598178L;

    /**
     *
     */
    private Long id;

    /**
     *
     */
    private Long roleId;

    /**
     *
     */
    private Long deptId;

}

