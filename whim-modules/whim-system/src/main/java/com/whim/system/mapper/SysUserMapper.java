package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统用户持久化及只读授权身份查询。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /** 判断账号是否仍承担有效岗位或部门的负责人职责。 */
    @DataPermissionMetadata
    boolean hasOrganizationOwnershipReferences(@Param("userId") Long userId);
    /** 仅供授权解析读取账号状态和主部门，不返回密码等业务信息。 */
    @DataPermissionMetadata
    SysUser selectAuthorizationUser(@Param("userId") Long userId);

    /** 业务写入中锁定目标用户，仍受当前注解的数据范围限制。 */
    SysUser selectForUpdate(@Param("userId") Long userId);
}

