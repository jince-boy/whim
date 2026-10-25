package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 角色菜单权限关联映射。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
