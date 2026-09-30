package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 精确功能授权及权限变更影响范围查询。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /** 查询账号通过有效角色获得的操作权限码。 */
    @DataPermissionMetadata
    Set<String> selectPermissionCodeSetByUserId(@Param("userId") Long userId);

    /** 查询权限变更需要撤销会话的账号。 */
    @DataPermissionMetadata
    Set<Long> selectUserIdSetByPermissionId(@Param("permissionId") Long permissionId);
}

