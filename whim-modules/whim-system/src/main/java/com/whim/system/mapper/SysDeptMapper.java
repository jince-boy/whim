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
 * @description 数据权限使用的有效部门层级查询。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /** 从非空候选集合中筛选完整祖先链有效的部门，不返回断链或循环节点。 */
    @DataPermissionMetadata
    Set<Long> selectValidDepartmentIds(@Param("departmentIds") Set<Long> departmentIds);

    /** 仅供权限解析读取沿有效父子链可达的部门ID。 */
    @DataPermissionMetadata
    Set<Long> selectActiveDescendantIds(@Param("deptId") Long deptId);
}

