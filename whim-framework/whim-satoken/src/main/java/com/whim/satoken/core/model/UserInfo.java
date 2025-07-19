package com.whim.satoken.core.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author jince
 * @date 2025/7/8 12:39
 * @description 用户信息
 */
@Data
public class UserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 权限
     */
    private Set<String> permissionCodeSet;
    /**
     * 角色权限
     */
    private Set<String> roleCodeSet;

    /**
     * 角色信息
     */
    private List<RoleInfo> roleInfoList;
}
