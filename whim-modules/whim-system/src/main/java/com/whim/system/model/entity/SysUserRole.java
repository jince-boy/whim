package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 用户角色关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserRole extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 913154846071904294L;

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

