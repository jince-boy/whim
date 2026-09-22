package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户租户关联表服务接口
 */
public interface ISysUserTenantService extends IService<SysUserTenant> {

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 租户ID集合
     */
    Set<Long> getTenantIdsByUserId(Long userId);

    /**
     * 查询用户当前可访问的租户。
     *
     * @param userId 用户ID
     * @return 可访问租户列表
     */
    List<SysTenant> getTenantListByUserId(Long userId);
}

