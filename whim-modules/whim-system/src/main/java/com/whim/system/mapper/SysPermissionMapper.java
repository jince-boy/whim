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
    /**
     * 批量查询角色关联的有效权限。
     *
     * @param roleIds 已核验的有效角色id集合，不能为空
     * @return 角色与有效权限的关联列表
     */
    @DataPermissionMetadata
    List<RolePermissionDTO> getRolePermissionsByRoleIds(@Param("roleIds") Set<Long> roleIds);

    /**
     * 锁定菜单维护配置行，避免并发修改破坏菜单层级。
     *
     * @return 菜单维护配置id，配置不存在时返回null
     */
    @DataPermissionMetadata
    Long lockMenuTree();

    /**
     * 查询受菜单权限变更影响的用户。
     *
     * @param permissionId 菜单权限id
     * @return 关联用户和超级管理员的用户id集合
     */
    @DataPermissionMetadata
    Set<Long> getUserIdsByPermissionId(@Param("permissionId") Long permissionId);
}

