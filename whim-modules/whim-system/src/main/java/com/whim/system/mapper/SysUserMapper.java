package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.post.SysPostMemberVO;
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
     * 分页查询岗位成员或未分配的正常用户，用户范围由当前操作的数据权限过滤。
     *
     * @param page 分页参数
     * @param query 岗位ID、成员类型及用户筛选条件
     * @return 用户分页结果
     */
    Page<SysPostMemberVO> getPostMemberPage(Page<SysPostMemberVO> page,
                                          @Param("query") SysPostMemberQueryDTO query);

    /**
     * 查询部门移动或启停影响的主部门用户、岗位成员和自定义数据范围角色用户。
     *
     * @param departmentIds 发生变化的非空部门ID集合
     * @return 需要重新加载部门授权信息的用户ID集合
     */
    @DataPermissionMetadata
    Set<Long> getAffectedUserIdsByDepartments(@Param("departmentIds") Set<Long> departmentIds);

    /**
     * 登录时批量读取角色自定义部门关系，保留每个角色对应的部门范围。
     *
     * @param roleIds 自定义部门范围的角色ID集合
     * @return 角色与部门的关联列表
     */
    @DataPermissionMetadata
    List<SysRoleDept> getRoleDepartmentsByRoleIds(@Param("roleIds") Set<Long> roleIds);
}

