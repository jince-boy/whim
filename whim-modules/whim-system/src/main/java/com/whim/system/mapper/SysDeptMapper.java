package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门持久化接口。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /**
     * 查询完整的下级部门ID，用于检查层级变更是否覆盖全部受影响部门。
     *
     * @param id 部门ID
     * @return 所有未删除的下级部门ID，不包含当前部门
     */
    @DataPermissionMetadata
    Set<Long> getDescendantDeptIds(@Param("id") Long id);

    /**
     * 查询祖先链中正常且未删除的部门数量，不受操作者可见范围影响。
     *
     * @param departmentIds 待校验的非空部门ID集合
     * @return 正常且未删除的部门数量
     */
    @DataPermissionMetadata
    long getEnabledDeptCountByIds(@Param("departmentIds") Set<Long> departmentIds);

    /**
     * 查询阻止部门删除的关联，包含操作者不可见的业务记录。
     *
     * @param id 部门ID
     * @return 无法删除的中文原因，没有关联时返回null
     */
    @DataPermissionMetadata
    String getDeptDeleteBlockReason(@Param("id") Long id);

    /**
     * 查询主部门、岗位部门或自定义角色范围引用指定部门的用户。
     *
     * @param departmentIds 发生变化的非空部门ID集合
     * @return 需要重新加载部门授权信息的用户ID集合
     */
    @DataPermissionMetadata
    Set<Long> getUserIdsByDepartments(@Param("departmentIds") Set<Long> departmentIds);

    /**
     * 查询因祖先部门的下级范围变化而需要重新登录的用户。
     *
     * @param ancestorIds 受影响的祖先部门ID集合，不包含根标识0
     * @return 拥有本部门及下级范围角色的受影响用户ID集合
     */
    @DataPermissionMetadata
    Set<Long> getUserIdsByDepartmentAncestors(@Param("ancestorIds") Set<Long> ancestorIds);

    /**
     * 批量替换下级部门的祖先路径前缀，仅修改路径和框架自动填充的审计字段。
     *
     * @param department 已更新的部门实体，使用实体参数触发审计字段自动填充
     * @param previousPath 变更前包含当前部门ID的完整路径
     * @return 已更新的下级部门数量
     */
    int updateDeptDescendantAncestors(@Param(Constants.ENTITY) SysDept department,
                                     @Param("previousPath") String previousPath);
}
