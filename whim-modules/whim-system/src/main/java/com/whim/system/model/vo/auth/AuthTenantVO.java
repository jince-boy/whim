package com.whim.system.model.vo.auth;

import com.whim.system.model.entity.SysTenant;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 登录用户可选择的租户信息。
 */
@Data
public class AuthTenantVO {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 将租户实体转换为认证租户响应对象。
     *
     * @param tenant 租户实体
     * @return 认证租户响应对象
     */
    public static AuthTenantVO from(SysTenant tenant) {
        AuthTenantVO tenantVO = new AuthTenantVO();
        tenantVO.setTenantId(tenant.getId());
        tenantVO.setTenantCode(tenant.getTenantCode());
        tenantVO.setCompanyName(tenant.getCompanyName());
        return tenantVO;
    }
}
