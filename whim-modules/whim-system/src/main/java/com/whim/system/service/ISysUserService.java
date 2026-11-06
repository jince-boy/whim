package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.user.SysUserAddDTO;
import com.whim.system.model.dto.user.SysUserPasswordDTO;
import com.whim.system.model.dto.user.SysUserQueryDTO;
import com.whim.system.model.dto.user.SysUserRoleDTO;
import com.whim.system.model.dto.user.SysUserUpdateDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.user.SysUserDetailVO;
import com.whim.system.model.vo.user.SysUserRolesVO;
import com.whim.system.model.vo.user.SysUserVO;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户管理及登录认证信息构建。
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 按部门分支和用户信息分页查询可见用户。
     *
     * @param query 用户筛选及分页条件
     * @return 用户分页
     */
    PageDataVO<SysUserVO> getUserPage(SysUserQueryDTO query);

    /**
     * 查询用户编辑详情和当前岗位。
     *
     * @param id 用户ID
     * @return 用户编辑详情
     */
    SysUserDetailVO getUserDetail(Long id);

    /**
     * 查询用户当前角色和可分配的角色。
     *
     * @param id 用户ID
     * @return 当前角色及候选项
     */
    SysUserRolesVO getUserRoles(Long id);

    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    SysUser getUserById(Long id);

    /**
     * 登录阶段按规范化用户名查询账号。
     *
     * @param username 用户名
     * @return 用户账号，不存在时返回null
     */
    SysUser getByUsername(String username);

    /**
     * 查询部门移动或启停影响的主部门用户、岗位成员和自定义数据范围角色用户。
     *
     * @param departmentIds 发生变化的非空部门ID集合
     * @return 需要重新加载部门授权信息的用户ID集合
     */
    Set<Long> getAffectedUserIdsByDepartments(Set<Long> departmentIds);

    /**
     * 内部查询祖先部门中拥有本部门及下级范围角色的用户，用于组织变更后的会话失效。
     *
     * @param ancestorIds 受影响的祖先部门ID集合，不包含根标识0
     * @return 主部门位于指定祖先中且拥有启用的下级范围角色的未删除用户ID集合
     */
    Set<Long> getUserIdsByDepartmentAncestors(Set<Long> ancestorIds);

    /**
     * 新增用户并保存岗位任职关系。
     *
     * @param parameter 用户新增参数
     */
    void addUser(SysUserAddDTO parameter);

    /**
     * 修改用户及岗位，提交后失效登录会话。
     *
     * @param parameter 用户ID及完整修改字段
     */
    void updateUser(SysUserUpdateDTO parameter);

    /**
     * 修改用户密码并在事务提交后使其全部会话失效。
     *
     * @param parameter 用户ID及新密码
     */
    void resetUserPassword(SysUserPasswordDTO parameter);

    /**
     * 校验角色可授予范围后更新用户角色。
     *
     * @param parameter 用户ID及完整角色集合
     */
    void assignUserRoles(SysUserRoleDTO parameter);

    /**
     * 删除用户及关联关系，禁止删除当前账号或仍担任部门负责人的用户。
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 构建登录账号的部门、岗位、角色、精确权限及角色部门范围快照。
     *
     * @param user 登录用户
     * @return 包含部门、岗位和角色授权范围的登录信息
     */
    UserInfo buildUserInfo(SysUser user);
}
