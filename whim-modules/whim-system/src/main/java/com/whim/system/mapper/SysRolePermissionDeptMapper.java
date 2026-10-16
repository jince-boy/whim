package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.dto.permission.DataScopeGrantDTO;
import com.whim.system.model.entity.SysRolePermissionDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 操作级自定义部门范围查询。
 */
@Mapper
public interface SysRolePermissionDeptMapper extends BaseMapper<SysRolePermissionDept> {
    /** 批量读取非空授权集合的覆盖部门，严格匹配每个角色与操作的组合。 */
    @DataPermissionMetadata
    Set<Long> selectDepartmentIdsByGrants(@Param("grants") List<DataScopeGrantDTO> grants);
}
