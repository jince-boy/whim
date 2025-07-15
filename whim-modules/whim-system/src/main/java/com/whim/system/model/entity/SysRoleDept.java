package com.whim.system.model.entity;

import com.whim.mybatis.core.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色与部门关联表(SysRoleDept)实体类
 *
 * @author Jince
 * @since 2025-07-10 16:12:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRoleDept extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 262547053573903488L;
    private Long id;
    private Long roleId;
    private Long deptId;
}
