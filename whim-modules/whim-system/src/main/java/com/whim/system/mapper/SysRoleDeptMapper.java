package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色自定义部门范围查询。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /** 批量读取非空角色集合的自定义部门，部门有效性由范围服务统一核验。 */
    @DataPermissionMetadata
    Set<Long> selectDepartmentIdsByRoleIds(@Param("roleIds") Set<Long> roleIds);
}

