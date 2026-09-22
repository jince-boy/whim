package com.whim.system.model.vo.auth;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 当前登录用户的租户选择列表响应参数。
 */
@Data
public class AuthTenantListVO {

    /**
     * 当前租户ID
     */
    private Long currentTenantId;

    /**
     * 默认租户ID
     */
    private Long defaultTenantId;

    /**
     * 可选择租户列表
     */
    private List<AuthTenantVO> tenantList = new ArrayList<>();
}
