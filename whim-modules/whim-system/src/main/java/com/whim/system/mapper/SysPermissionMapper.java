package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统权限菜单表数据库访问层
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 查询用户已启用权限编码列表。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @return 权限编码集合
     */
    Set<String> selectPermissionCodeSetByUserIdAndTenantId(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 查询当前拥有指定权限的用户ID集合。
     *
     * @param permissionId 权限ID
     * @return 用户ID集合
     */
    Set<Long> selectUserIdSetByPermissionId(@Param("permissionId") Long permissionId);
}

