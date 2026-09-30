package com.whim.system.model.vo.role;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 系统角色响应。
 */
@Data
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Integer dataScope;
    private Integer status;
}
