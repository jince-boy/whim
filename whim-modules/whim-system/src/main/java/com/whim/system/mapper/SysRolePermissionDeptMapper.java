package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRolePermissionDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 操作级自定义部门范围查询。
 */
@Mapper
public interface SysRolePermissionDeptMapper extends BaseMapper<SysRolePermissionDept> {
    /** 返回指定角色与操作的自定义部门ID。 */
    Set<Long> selectDepartmentIds(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
