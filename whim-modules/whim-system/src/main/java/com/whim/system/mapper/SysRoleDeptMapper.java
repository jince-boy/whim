package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色默认自定义部门范围查询。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /** 返回角色默认自定义部门ID，祖先链有效性由权限解析器核验。 */
    Set<Long> selectDepartmentIds(@Param("roleId") Long roleId);
}

