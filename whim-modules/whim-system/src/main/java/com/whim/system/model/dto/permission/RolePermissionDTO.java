package com.whim.system.model.dto.permission;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/21
 * @description 登录角色授予的有效操作权限。
 */
@Data
public class RolePermissionDTO {
    private Long roleId;
    private String permissionCode;
}
