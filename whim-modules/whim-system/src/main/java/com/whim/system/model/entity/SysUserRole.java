package com.whim.system.model.entity;

import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 用户与角色的关联关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserRole extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 关联ID。 */
    private Long id;
    /** 用户ID。 */
    private Long userId;
    /** 角色ID。 */
    private Long roleId;
}
