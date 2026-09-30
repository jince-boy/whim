package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 有效角色、精确操作授权与受影响账号查询。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /** 查询账号当前有效角色。 */
    @DataPermissionMetadata
    List<RoleInfo> selectRoleInfoListByUserId(@Param("userId") Long userId);

    /** 查询实际授予本次操作的角色；覆盖范围优先于角色默认范围。 */
    @DataPermissionMetadata
    List<DataScopeGrantDTO> selectAuthorizedDataScopeRoles(@Param("userId") Long userId,
                                                         @Param("permissionCode") String permissionCode);

    /** 查询指定角色的操作与有效数据范围，用于授权边界检查。 */
    @DataPermissionMetadata
    List<DataScopeGrantDTO> selectRoleGrants(@Param("roleId") Long roleId);

    /** 判断有效账号是否持有有效超级管理员角色。 */
    @DataPermissionMetadata
    Boolean selectSuperAdministratorFlag(@Param("userId") Long userId);

    /** 识别目标账号的管理员角色，即使该账号当前已停用。 */
    @DataPermissionMetadata
    Boolean selectHasSuperAdministratorRole(@Param("userId") Long userId);

    /** 获取目标账号持有的角色，不因账号停用而遗漏待恢复的授权。 */
    @DataPermissionMetadata
    List<SysRole> selectAdministrativeRolesByUserId(@Param("userId") Long userId);

    /** 查询角色变更需要撤销会话的账号。 */
    @DataPermissionMetadata
    Set<Long> selectUserIdsByRole(@Param("roleId") Long roleId);

    /** 串行保护管理员成员变更，调用方须处于业务事务中。 */
    @DataPermissionMetadata
    Long lockSuperAdministratorRole();

    /** 统计有效超级管理员账号，防止移除最后一个管理员。 */
    @DataPermissionMetadata
    long countActiveSuperAdministrators();
}

