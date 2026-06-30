package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色与部门关联表数据库访问层
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
}

