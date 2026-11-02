package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.core.auth.model.PostInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统用户持久化及登录授权信息查询。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 登录时读取用户关联的有效岗位，部门有效性在构建上下文时统一核验。
     *
     * @param userId 用户ID
     * @return 用户关联的有效岗位信息
     */
    @DataPermissionMetadata
    List<PostInfo> getPostInfoListByUserId(@Param("userId") Long userId);

    /**
     * 登录时读取自身及所有祖先均启用、未删除的部门。
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
     * 登录时批量读取角色自定义部门关系，保留每个角色对应的部门范围。
     *
     * @param roleIds 自定义部门范围的角色ID集合
     * @return 角色与部门的关联列表
     */
    @DataPermissionMetadata
    List<SysRoleDept> getRoleDepartmentsByRoleIds(@Param("roleIds") Set<Long> roleIds);
}

