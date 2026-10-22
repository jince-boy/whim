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

    /** 登录时读取完整祖先链有效的候选部门，不返回断链或循环节点。 */
    @DataPermissionMetadata
    List<DepartmentInfo> selectValidDepartments(@Param("departmentIds") Set<Long> departmentIds);

    /** 登录时从已验证的主部门展开有效下级，结果包含主部门。 */
    @DataPermissionMetadata
    Set<Long> selectActiveDescendantIds(@Param("deptId") Long deptId);
}

