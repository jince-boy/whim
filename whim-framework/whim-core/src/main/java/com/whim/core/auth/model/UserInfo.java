package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
     * 真实姓名
     */
    private String name;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long deptId;

    /** 有效主部门信息，部门或祖先链无效时为空。 */
    private DepartmentInfo departmentInfo;

    /** 有效岗位列表。 */
    private List<PostInfo> postInfoList = new ArrayList<>();

    /**
     * 账号体系
     */
    private String loginType;

    /**
     * 权限编码集合
     */
    private Set<String> permissionCodeSet = new LinkedHashSet<>();

    /** 角色直接授予的菜单ID，供导航使用；不通过父子关系扩大操作权限。 */
    private Set<Long> menuIdSet = new LinkedHashSet<>();

    /** 精确权限码与授予该权限的角色ID集合，登录时构建。 */
    private Map<String, Set<Long>> permissionRoleIds = new LinkedHashMap<>();

    /** 各角色允许的有效部门ID，登录时完成自定义部门校验及下级部门展开。 */
    private Map<Long, Set<Long>> roleDepartmentIds = new LinkedHashMap<>();

    /**
     * 角色编码集合
     */
    private Set<String> roleCodeSet = new LinkedHashSet<>();

    /**
     * 角色信息列表
     */
    private List<RoleInfo> roleInfoList = new ArrayList<>();
}
