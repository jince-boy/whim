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

    /**
     * 登录或归属校验时读取自身及所有祖先均启用、未删除的部门。
     *
     * @param departmentIds 候选部门ID集合
     * @return 有效部门信息
     */
    @DataPermissionMetadata
    List<DepartmentInfo> getValidDepartmentsByIds(@Param("departmentIds") Set<Long> departmentIds);

    /**
     * 登录时读取有效的本部门及下级，排除停用或删除的分支。
     *
     * @param deptId 主部门ID
     * @return 有效部门ID集合
     */
    @DataPermissionMetadata
    Set<Long> getValidDepartmentAndDescendantIds(@Param("deptId") Long deptId);

    /**
     * 读取用于完整性校验的部门及下级，结果仅供服务内部校验和路径维护。
     *
     * @param deptId 部门ID
     * @return 包含自身和停用下级的未删除部门列表
     */
    @DataPermissionMetadata
    List<SysDept> getDepartmentBranch(@Param("deptId") Long deptId);

    /**
     * 读取上级部门路径，供服务内部校验层级，不能直接作为接口响应。
     *
     * @param deptId 上级部门ID
     * @return 未删除的上级部门，不存在时返回null
     */
    @DataPermissionMetadata
    SysDept getDepartmentReferenceById(@Param("deptId") Long deptId);

    /**
     * 查询部门变化影响的主部门用户、岗位成员及数据范围持有者。
     *
     * @param departmentIds 变化分支的部门ID集合
     * @param ancestorIds 变化前后祖先部门ID集合
     * @return 应在事务提交后失效会话的用户ID集合
     */
    @DataPermissionMetadata
    Set<Long> getUserIdsByDepartmentChange(@Param("departmentIds") Set<Long> departmentIds,
                                         @Param("ancestorIds") Set<Long> ancestorIds);
}

