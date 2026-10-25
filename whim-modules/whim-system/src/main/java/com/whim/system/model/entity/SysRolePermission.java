package com.whim.system.model.entity;

import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 角色菜单权限关联，随授权撤销物理删除。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    private Long id;
    /** 角色ID。 */
    private Long roleId;
    /** 菜单权限ID。 */
    private Long permissionId;
}
