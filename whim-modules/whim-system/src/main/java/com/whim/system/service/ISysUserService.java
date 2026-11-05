package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.system.model.entity.SysUser;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号查询及认证信息构建。
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    SysUser getUserById(Long id);

    /**
     * 登录阶段按规范化用户名查询账号。
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
     * 构建登录账号的部门、岗位、角色、精确权限及角色部门范围快照。
     *
     * @param user 登录用户
     * @return 包含部门、岗位和角色授权范围的登录信息
     */
    UserInfo buildUserInfo(SysUser user);
}
