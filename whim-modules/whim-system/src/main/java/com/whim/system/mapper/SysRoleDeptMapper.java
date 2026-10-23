package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色自定义部门范围查询。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /** 登录时批量读取自定义部门关系，保留角色ID，避免不同操作的范围混合。 */
    @DataPermissionMetadata
    List<SysRoleDept> getRoleDepartmentsByRoleIds(@Param("roleIds") Set<Long> roleIds);
}

