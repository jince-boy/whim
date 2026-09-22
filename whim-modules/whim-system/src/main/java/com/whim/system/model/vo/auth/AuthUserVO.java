package com.whim.system.model.vo.auth;

import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
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
     * 可访问租户ID集合
     */
    private Set<Long> tenantIds = new LinkedHashSet<>();

    /**
     * 默认进入租户ID
     */
    private Long defaultTenantId;

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

    /**
     * 将认证上下文转换为用户响应对象。
     *
     * @param userInfo 当前登录用户信息
     * @return 当前登录用户响应对象
     */
    public static AuthUserVO from(UserInfo userInfo) {
        AuthUserVO userVO = new AuthUserVO();
        userVO.setUserId(userInfo.getUserId());
        userVO.setUsername(userInfo.getUsername());
        userVO.setName(userInfo.getName());
        userVO.setAvatar(userInfo.getAvatar());
        userVO.setDeptId(userInfo.getDeptId());
        userVO.setTenantIds(new LinkedHashSet<>(userInfo.getTenantIds()));
        userVO.setDefaultTenantId(userInfo.getDefaultTenantId());
        userVO.setLoginType(userInfo.getLoginType());
        userVO.setRoleCodeSet(new LinkedHashSet<>(userInfo.getRoleCodeSet()));
        userVO.setPermissionCodeSet(new LinkedHashSet<>(userInfo.getPermissionCodeSet()));
        userVO.setRoleInfoList(List.copyOf(userInfo.getRoleInfoList()));
        return userVO;
    }
}
