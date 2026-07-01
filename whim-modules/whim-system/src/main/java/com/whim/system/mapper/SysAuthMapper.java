package com.whim.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/01
 * @description 系统认证授权数据库访问层
 */
@Mapper
public interface SysAuthMapper {

    /**
     * 查询用户已启用角色编码列表。
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodeListByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已启用权限编码列表。
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> selectPermissionCodeListByUserId(@Param("userId") Long userId);
}
