package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 菜单权限(SysPermission)表数据库访问层
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 通过用户id获取权限标识列表
     *
     * @param userId 用户id
     * @return 权限标识列表
     */
    Set<String> getPermissionCodeByUserId(@Param("userId") Long userId);
}

