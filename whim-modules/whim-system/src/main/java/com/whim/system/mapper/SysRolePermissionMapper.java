package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色权限关联表数据库访问层
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    /**
     * 创建或恢复角色权限绑定。
     *
     * @param id 新绑定ID
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @param tenantId 租户ID
     * @param operatorId 操作人ID
     */
    void upsertBinding(@Param("id") Long id, @Param("roleId") Long roleId,
                       @Param("permissionId") Long permissionId, @Param("tenantId") Long tenantId,
                       @Param("operatorId") Long operatorId);
}

