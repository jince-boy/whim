package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色部门关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleDept extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 864801869260635659L;

    /**
     * id
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;


}

