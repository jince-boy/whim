package com.whim.satoken.core.model;

import lombok.Data;

/**
 * @author jince
 * date: 2025/7/10 12:40
 * description: 角色信息
 */
@Data
public class RoleInfo {
    private Long id;
    private String name;
    private String code;
    private String dataScope;
}
