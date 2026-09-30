package com.whim.system.model.vo.role;

import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 系统角色数据范围及自定义部门响应。
 */
@Data
public class RoleDataScopeVO {
    private Long roleId;
    private Integer dataScope;
    private Set<Long> deptIds;
    /** 操作覆盖范围的权限ID，角色默认范围响应时为空。 */
    private Long permissionId;
}
