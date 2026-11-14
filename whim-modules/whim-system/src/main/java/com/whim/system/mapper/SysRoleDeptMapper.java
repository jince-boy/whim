package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色自定义数据范围关联持久化接口。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
}
