package com.whim.system.model.vo.auth;

import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/21
 * @description 当前登录用户响应参数。
 */
@Data
@AutoMapper(target = UserInfo.class, convertGenerate = false)
public class AuthUserVO {
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
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 当前可选择的有效租户
     */
    private List<AuthTenantVO> tenantList = List.of();

    /**
     * 默认进入租户ID
     */
    private Long defaultTenantId;

    /**
     * 当前操作租户ID
     */
    private Long currentTenantId;

    /**
     * 账号体系
     */
    private String loginType;

    /**
     * 角色编码集合
     */
    private Set<String> roleCodeSet = new LinkedHashSet<>();

    /**
     * 权限编码集合
     */
    private Set<String> permissionCodeSet = new LinkedHashSet<>();

    /**
     * 角色信息列表
     */
    private List<RoleInfo> roleInfoList;

}
