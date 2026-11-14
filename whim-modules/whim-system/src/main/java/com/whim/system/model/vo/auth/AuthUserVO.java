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

    /** 独立的超级管理员身份，为true时默认拥有全部功能和数据权限。 */
    private boolean superAdministrator;

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

    /** 当前有效主部门名称，未分配或部门不可用时为空。 */
    private String deptName;

    /** 当前有效岗位名称。 */
    private List<String> postNames;

    /** 联系手机，未填写时为空。 */
    private String phone;

    /** 联系邮箱，未填写时为空。 */
    private String email;

    /** 性别字典编码。 */
    private Integer gender;

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
