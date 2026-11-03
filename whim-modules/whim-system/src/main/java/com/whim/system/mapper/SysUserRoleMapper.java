package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 用户角色关联的基础持久化接口。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
