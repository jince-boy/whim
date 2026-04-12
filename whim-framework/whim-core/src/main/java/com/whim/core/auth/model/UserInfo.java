package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/04/12
 * @description 当前登录用户信息。
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
     * 权限编码集合
     */
    private Set<String> permissionCodeSet;

    /**
     * 角色编码集合
     */
    private Set<String> roleCodeSet;

    /**
     * 角色信息集合
     */
    private List<RoleInfo> roleInfoList;
}
