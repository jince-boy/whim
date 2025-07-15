package com.whim.system.model.entity;

import com.whim.mybatis.core.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户-角色关联表(SysUserRole)实体类
 *
 * @author Jince
 * @since 2025-06-27 16:29:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserRole extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 243089234935061128L;
    /**
     * 关联ID
     */
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 角色ID
     */
    private Long roleId;
}
