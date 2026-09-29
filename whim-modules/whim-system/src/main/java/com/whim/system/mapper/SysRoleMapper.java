package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色表数据库访问层
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 查询用户已启用角色的完整信息。
     *
     * @param userId 用户ID
     * @param tenantId 当前租户ID，平台上下文时为空
     * @return 角色信息列表
     */
    List<RoleInfo> selectRoleInfoListByUserIdAndTenantId(
            @Param("userId") Long userId,
            @Param("tenantId") Long tenantId
    );

    /** 查询当前租户中实际授予本次权限且受到套餐允许的有效角色。 */
    List<RoleInfo> selectAuthorizedDataScopeRoles(@Param("userId") Long userId,
                                                   @Param("tenantId") Long tenantId,
                                                   @Param("permissionCode") String permissionCode);

    /**
     * 判断用户是否拥有全局超级管理员角色。
     *
     * @param userId 用户ID
     * @return true 表示拥有超级管理员角色
     */
    Boolean selectSuperAdministratorFlag(@Param("userId") Long userId);
}

