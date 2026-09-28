package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysTenant;

import java.util.List;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表服务接口
 */
public interface ISysTenantService extends IService<SysTenant> {

    /**
     * 查询全部当前可用租户。
     *
     * @return 可用租户列表
     */
    List<SysTenant> getAvailableTenantList();

    /**
     * 查询全部当前可用租户ID。
     *
     * @return 可用租户ID集合
     */
    Set<Long> getAvailableTenantIds();

    /**
     * 取得当前有效租户并核对当前用户的成员身份。
     *
     * @return 当前租户
     */
    SysTenant getRequiredCurrentTenant();

    /**
     * 要求当前用户拥有平台超级管理员权限。
     */
    void requirePlatformAdministrator();

    /**
     * 修改租户状态。
     *
     * @param tenantId 租户ID
     * @param status 状态
     */
    void setTenantStatus(Long tenantId, Integer status);

    /**
     * 修改租户使用的套餐。
     *
     * @param tenantId 租户ID
     * @param packageId 套餐ID
     */
    void setTenantPackage(Long tenantId, Long packageId);
}

