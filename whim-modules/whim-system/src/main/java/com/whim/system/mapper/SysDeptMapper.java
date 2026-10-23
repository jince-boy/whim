package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录上下文使用的有效部门及层级查询。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /** 登录时读取自身及所有祖先均启用、未删除的候选部门。 */
    @DataPermissionMetadata
    List<DepartmentInfo> getValidDepartmentsByIds(@Param("departmentIds") Set<Long> departmentIds);

    /** 登录时读取有效的本部门及下级，排除停用或删除部门的整条下级分支。 */
    @DataPermissionMetadata
    Set<Long> getValidDepartmentAndDescendantIds(@Param("deptId") Long deptId);
}

