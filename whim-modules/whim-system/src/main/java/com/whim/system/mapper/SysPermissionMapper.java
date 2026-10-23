package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.dto.permission.RolePermissionDTO;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录权限及数据权限使用的授权元数据查询。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /** 批量读取非空有效角色集合的权限关系，角色有效性由登录流程核验。 */
    @DataPermissionMetadata
    List<RolePermissionDTO> getRolePermissionsByRoleIds(@Param("roleIds") Set<Long> roleIds);
}

