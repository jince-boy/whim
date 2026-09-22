package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户租户关联表数据库访问层
 */
@Mapper
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 租户ID集合
     */
    Set<Long> selectTenantIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户当前可访问的租户。
     *
     * @param userId 用户ID
     * @return 可访问租户列表
     */
    List<SysTenant> selectTenantListByUserId(@Param("userId") Long userId);
}

