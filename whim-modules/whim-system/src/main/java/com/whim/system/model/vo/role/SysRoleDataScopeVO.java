package com.whim.system.model.vo.role;

import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色统一数据范围回显，部门ID按实际选择返回。
 */
@Data
public class SysRoleDataScopeVO {
    private Long id;
    private Integer dataScope;
    private List<Long> deptIds;
}
