package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/06
 * @description 当前登录用户上下文通用信息。
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
     * 账号体系
     */
    private String loginType;

    /**
     * 权限编码集合
     */
    private Set<String> permissionCodeSet = new LinkedHashSet<>();

    /**
     * 角色编码集合
     */
    private Set<String> roleCodeSet = new LinkedHashSet<>();

    /**
     * 角色信息列表
     */
    private List<RoleInfo> roleInfoList = new ArrayList<>();
}
