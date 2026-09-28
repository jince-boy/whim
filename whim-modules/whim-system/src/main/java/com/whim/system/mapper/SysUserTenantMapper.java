package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.vo.tenant.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户租户关联表数据库访问层
 */
@Mapper
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {

    /**
     * 判断用户是否为指定租户的有效成员。
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return true 表示成员有效
     */
    Boolean selectActiveMemberFlag(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 租户ID集合
     */
    Set<Long> selectTenantIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户当前可访问的租户。
     *
     * @param userId 用户ID
     * @return 可访问租户列表
     */
    List<SysTenant> selectTenantListByUserId(@Param("userId") Long userId);

    /**
     * 查询指定租户的成员摘要。
     *
     * @param tenantId 租户ID
     * @return 成员列表
     */
    List<MemberVO> selectMemberList(@Param("tenantId") Long tenantId);

    /**
     * 查询租户内所有未删除的成员ID，用于授权失效。
     *
     * @param tenantId 租户ID
     * @return 用户ID集合
     */
    Set<Long> selectMemberUserIds(@Param("tenantId") Long tenantId);

    /**
     * 查询使用指定套餐的租户成员，用于套餐变更后撤销旧授权。
     *
     * @param packageId 套餐ID
     * @return 用户ID集合
     */
    Set<Long> selectMemberUserIdsByPackageId(@Param("packageId") Long packageId);
}

