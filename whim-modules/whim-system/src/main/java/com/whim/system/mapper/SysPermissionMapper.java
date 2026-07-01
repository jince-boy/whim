package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 权限表(菜单和按钮权限)数据库访问层
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询用户已启用权限编码列表。
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> selectPermissionCodeSetByUserId(@Param("userId") Long userId);
}
