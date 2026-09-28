package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表数据库访问层
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    /**
     * 查询当前仍可使用且套餐启用的租户。
     *
     * @param tenantId 租户ID
     * @return 有效租户，不满足条件时为空
     */
    SysTenant selectActiveTenantById(@Param("tenantId") Long tenantId);

    /**
     * 查询全部当前可用租户。
     *
     * @return 可用租户列表
     */
    List<SysTenant> selectAvailableTenantList();

    /**
     * 查询全部当前可用租户ID。
     *
     * @return 可用租户ID集合
     */
    Set<Long> selectAvailableTenantIds();
}

