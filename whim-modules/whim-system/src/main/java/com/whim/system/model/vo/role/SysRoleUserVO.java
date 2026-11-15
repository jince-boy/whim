package com.whim.system.model.vo.role;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/15
 * @description 角色用户列表，仅包含分配用户所需的展示字段。
 */
@Data
public class SysRoleUserVO {
    private Long id;
    private String username;
    private String name;
    private String deptName;
    private String phone;
    private Integer status;
}
