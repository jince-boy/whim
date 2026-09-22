package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

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

