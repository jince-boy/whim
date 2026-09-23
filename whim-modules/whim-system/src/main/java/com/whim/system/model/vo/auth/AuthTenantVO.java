package com.whim.system.model.vo.auth;

import com.whim.system.model.entity.SysTenant;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.ReverseAutoMapping;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 登录用户可选择的租户信息。
 */
@Data
@AutoMapper(target = SysTenant.class, convertGenerate = false)
public class AuthTenantVO {

    /**
     * 租户ID
     */
    @ReverseAutoMapping(source = "id", target = "tenantId")
    private Long tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 企业名称
     */
    private String companyName;

}
