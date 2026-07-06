package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/07/06
 * @description 当前登录用户角色上下文通用信息。
 */
@Data
public class RoleInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 数据权限范围
     */
    private Integer dataScope;
}
