package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色操作授权数据库访问层。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}

