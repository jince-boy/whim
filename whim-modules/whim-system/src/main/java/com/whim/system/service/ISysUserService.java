package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.UserInfo;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.user.UserCreateDTO;
import com.whim.system.model.dto.user.UserUpdateDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.user.UserVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 统一账号、主部门及受数据权限保护的用户管理。
 */
public interface ISysUserService extends IService<SysUser> {
    /** 分页查询本次操作可见用户。 */
    PageDataVO<UserVO> pageUsers(PageQueryDTO query);
    /** 查询可见用户详情，不返回密码。 */
    UserVO getUser(Long userId);
    /** 登录阶段按规范化用户名查询有效账号。 */
    SysUser getByUsername(String username);
    /** 构建角色和精确权限快照。 */
    UserInfo buildUserInfo(SysUser user);
    /** 内部锁定本次注解范围可见的用户。 */
    SysUser getRequiredUserInScope(Long userId);
    /** 一次性校验全部用户存在且可见，按需校验状态并锁定。 */
    List<SysUser> requireUsersInScope(Set<Long> userIds, boolean active, boolean lock);
    /** 创建普通账号，不自动授予角色。 */
    Long createUser(UserCreateDTO request);
    /** 修改可见账号基础信息。 */
    void updateUser(Long userId, UserUpdateDTO request);
    /** 分配主部门并核验迁移后的角色范围。 */
    void setUserDepartment(Long userId, Long deptId);
    /** 修改状态，保护自身和最后一个有效管理员。 */
    void setUserStatus(Long userId, Integer status);
    /** 重置可见账号密码并撤销全部旧会话。 */
    void resetPassword(Long userId, String password);
    /** 软删除账号并移除角色、岗位关系。 */
    void deleteUser(Long userId);
}

