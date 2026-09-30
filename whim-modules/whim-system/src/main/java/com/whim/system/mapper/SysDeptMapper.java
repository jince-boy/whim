package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 部门结构授权元数据与引用完整性检查。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /** 仅供内部祖先链验证读取有效部门结构。 */
    @DataPermissionMetadata
    SysDept selectAuthorizationDepartment(@Param("deptId") Long deptId);

    /** 仅供权限解析读取沿有效父子链可达的部门ID。 */
    @DataPermissionMetadata
    Set<Long> selectActiveDescendantIds(@Param("deptId") Long deptId);

    /** 检查部门是否仍被组织节点、用户、岗位或数据范围引用。 */
    @DataPermissionMetadata
    boolean hasDepartmentReferences(@Param("deptId") Long deptId);
}

