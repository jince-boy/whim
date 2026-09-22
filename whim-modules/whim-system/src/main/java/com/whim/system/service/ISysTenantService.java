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
}

