package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/04/12
 * @description 当前登录用户关联的角色信息。
 */
@Data
public class RoleInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色标识
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 数据权限范围
     */
    private String dataScope;
}
