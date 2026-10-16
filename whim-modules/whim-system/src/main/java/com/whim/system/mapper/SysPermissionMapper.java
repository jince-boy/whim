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
    /** 读取有效页面或按钮的权限定义，供数据范围解析使用。 */
    @DataPermissionMetadata
    SysPermission selectAuthorizationPermission(@Param("permissionCode") String permissionCode);

    /** 查询账号通过有效角色获得的操作权限码。 */
    @DataPermissionMetadata
    Set<String> selectPermissionCodeSetByUserId(@Param("userId") Long userId);

    /** 查询权限变更需要撤销会话的账号。 */
    @DataPermissionMetadata
    Set<Long> selectUserIdSetByPermissionId(@Param("permissionId") Long permissionId);

    /** 按有效角色查询直接获得的菜单节点，不展开目录授权。 */
    @DataPermissionMetadata
    Set<Long> selectGrantedMenuIds(@Param("userId") Long userId);

}

