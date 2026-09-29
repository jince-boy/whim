package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户角色关联表数据库访问层
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 创建或恢复用户角色绑定。
     *
     * @param id 新绑定ID
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param tenantId 租户ID
     * @param operatorId 操作人ID
     */
    void upsertBinding(@Param("id") Long id, @Param("userId") Long userId,
                       @Param("roleId") Long roleId, @Param("tenantId") Long tenantId,
                       @Param("operatorId") Long operatorId);
}

