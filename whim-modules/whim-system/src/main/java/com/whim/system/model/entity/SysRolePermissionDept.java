package com.whim.system.model.entity;

import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 某个角色操作授权的自定义部门范围，独立于角色默认范围。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermissionDept extends BaseEntity {
    private Long id;
    private Long roleId;
    private Long permissionId;
    private Long deptId;
}
