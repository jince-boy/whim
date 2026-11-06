package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门持久化接口。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /**
     * 查询未删除的本部门及全部下级部门ID，供用户和岗位列表筛选使用。
     *
     * @param deptId 所选部门ID
     * @return 部门分支ID列表，包含停用部门；所选部门不存在或已删除时为空
     */
    @DataPermissionMetadata
    List<Long> getDeptAndDescendantIds(@Param("deptId") Long deptId);

    /**
     * 查询部门及其祖先链的有效状态，供岗位归属校验使用。
     *
     * @param id 部门ID
     * @return 0表示整条链启用，1表示部门或祖先链无效，部门自身不存在或已删除时返回null
     */
    @DataPermissionMetadata
    Integer getDeptEffectiveStatusById(@Param("id") Long id);

    /**
     * 判断部门是否被未删除角色的数据范围引用，关联查询排除已删除角色。
     *
     * @param id 部门ID
     * @return 是否存在有效的角色引用
     */
    @DataPermissionMetadata
    boolean getRoleReferenceExistsByDeptId(@Param("id") Long id);

    /**
     * 查询自身及所有祖先均启用、未删除的部门，供登录授权信息构建使用。
     *
     * @param departmentIds 候选部门ID集合
     * @return 有效部门信息
     */
    @DataPermissionMetadata
    List<DepartmentInfo> getValidDepartmentsByIds(@Param("departmentIds") Set<Long> departmentIds);

    /**
     * 查询有效的本部门及下级，排除停用或删除的分支，供登录授权信息构建使用。
     *
     * @param deptId 主部门ID
     * @return 有效部门ID集合
     */
    @DataPermissionMetadata
    Set<Long> getValidDepartmentAndDescendantIds(@Param("deptId") Long deptId);

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
