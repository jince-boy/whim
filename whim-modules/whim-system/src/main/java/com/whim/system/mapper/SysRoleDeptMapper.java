package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与部门关联表(SysRoleDept)表数据库访问层
 *
 * @author Jince
 * @since 2025-07-10 16:12:18
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

}

