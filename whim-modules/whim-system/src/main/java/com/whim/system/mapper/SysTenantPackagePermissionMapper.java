package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenantPackagePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户套餐权限关联表数据库访问层
 */
@Mapper
public interface SysTenantPackagePermissionMapper extends BaseMapper<SysTenantPackagePermission> {
    /**
     * 创建或恢复套餐权限绑定。
     *
     * @param id 新绑定ID
     * @param packageId 套餐ID
     * @param permissionId 权限ID
     * @param operatorId 操作人ID
     */
    void upsertBinding(@Param("id") Long id, @Param("packageId") Long packageId,
                       @Param("permissionId") Long permissionId, @Param("operatorId") Long operatorId);
}

