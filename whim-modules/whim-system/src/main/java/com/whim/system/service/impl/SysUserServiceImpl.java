package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.user.UserCreateDTO;
import com.whim.system.model.dto.user.UserUpdateDTO;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.vo.user.UserVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 全局唯一账号与单主部门管理，角色、岗位分别维护。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final ISysDeptService deptService;
    private final ISysUserRoleService userRoleService;
    private final ISysUserPostService userPostService;
    private final SysAuthorizationService authorizationService;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;

    /** 列表和总数在同一注解数据范围内执行。 */
    @Override
    @DataPermission(permission = "system:user:list",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public PageDataVO<UserVO> pageUsers(PageQueryDTO query) {
        return new PageDataVO<>(page(new Page<>(query.getPageNum(), query.getPageSize()),
                lambdaQuery().orderByAsc(SysUser::getId).getWrapper()).convert(this::toUserVO));
    }

    /** 按详情授权查询目标，不泄露其他范围内账号是否存在。 */
    @Override
    @DataPermission(permission = "system:user:detail",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public UserVO getUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new DataAccessDeniedException("用户不存在或不在本次操作范围内");
        }
        return toUserVO(user);
    }

    /** 用户名全局规范化，登录阶段不依赖业务数据权限。 */
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username.trim().toLowerCase(Locale.ROOT)).one();
    }

    /** 复用实体转换工具并获取当前有效角色和权限。 */
    @Override
    public UserInfo buildUserInfo(SysUser user) {
        UserInfo info = BeanConvertUtils.convert(user, UserInfo.class);
        info.setLoginType(AuthUserType.SYSTEM);
        List<RoleInfo> roles = roleService.getRoleInfoListByUserId(user.getId());
        info.setRoleInfoList(roles);
        info.setRoleCodeSet(roles.stream().map(RoleInfo::getRoleCode)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        info.setPermissionCodeSet(permissionService.getPermissionCodeSetByUserId(user.getId()));
        return info;
    }

    /** 在当前注解范围内锁定用户，调用方必须先建立受保护业务上下文。 */
    @Override
    public SysUser getRequiredUserInScope(Long userId) {
        DataPermissionContext.requiredScope();
        SysUser user = lambdaQuery().eq(SysUser::getId, userId).last("FOR UPDATE").one();
        if (user == null) {
            throw new DataAccessDeniedException("用户不存在或不在本次操作范围内");
        }
        return user;
    }

    /** 全部校验后再写入，不对不可见或停用用户悄悄进行部分处理。 */
    @Override
    public List<SysUser> requireUsersInScope(Set<Long> userIds, boolean active, boolean lock) {
        DataPermissionContext.requiredScope();
        if (userIds.isEmpty()) {
            return List.of();
        }
        var query = lambdaQuery().in(SysUser::getId, userIds).orderByAsc(SysUser::getId);
        if (lock) {
            query.last("FOR UPDATE");
        }
        List<SysUser> users = query.list();
        if (users.size() != userIds.size()
                || (active && users.stream().anyMatch(user -> user.getStatus() != 0))) {
            throw new DataAccessDeniedException("目标用户不存在、不可见或不可用");
        }
        return users;
    }

    /** 在授权部门创建账号，不隐式授予管理能力。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:create",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public Long createUser(UserCreateDTO request) {
        DataPermissionContext.checkOwnership("sys_user", null, request.getDeptId());
        deptService.getRequiredActiveDepartment(request.getDeptId());
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encodePassword(request.getPassword()));
        user.setDeptId(request.getDeptId());
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setRemark(request.getRemark());
        user.setStatus(0);
        save(user);
        return user.getId();
    }

    /** 仅更新基础信息，部门、密码和角色保持独立的操作权限。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:update",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void updateUser(Long userId, UserUpdateDTO request) {
        SysUser user = getRequiredUserInScope(userId);
        authorizationService.requireManageableUser(userId);
        if (!lambdaUpdate().eq(SysUser::getId, userId)
                .set(SysUser::getName, request.getName().trim()).set(SysUser::getAvatar, request.getAvatar())
                .set(SysUser::getEmail, request.getEmail()).set(SysUser::getPhone, request.getPhone())
                .set(SysUser::getGender, request.getGender()).set(SysUser::getRemark, request.getRemark())
                .set(SysUser::getUpdateBy, authenticationContext.getUserId())
                .set(SysUser::getUpdateTime, java.time.LocalDateTime.now()).update()) {
            throw new DataAccessDeniedException("用户已不可修改");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(user.getId()));
    }

    /** 校验原用户、新部门及迁移后角色范围，禁止普通管理员通过自行调岗扩大范围。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:department",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void setUserDepartment(Long userId, Long deptId) {
        SysUser user = getRequiredUserInScope(userId);
        authorizationService.requireManageableUser(userId);
        if (Objects.equals(userId, authenticationContext.getUserId())
                && !authorizationService.isSuperAdministrator()) {
            throw new DataAccessDeniedException("不能通过修改本人主部门改变授权范围");
        }
        DataPermissionContext.checkOwnership("sys_user", userId, deptId);
        deptService.getRequiredActiveDepartment(deptId);
        user.setDeptId(deptId);
        authorizationService.requireDelegableUserRoles(user);
        updateById(user);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 串行校验管理员成员变化，再启停账号并撤销会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:status",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void setUserStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        authorizationService.lockAdministratorMembership();
        SysUser user = getRequiredUserInScope(userId);
        authorizationService.requireManageableUser(userId);
        if (status == 1) {
            if (Objects.equals(userId, authenticationContext.getUserId())) {
                throw new DataAccessDeniedException("不能停用当前登录账号");
            }
            authorizationService.requireAdministratorCanBeRemoved(userId);
        } else {
            if (user.getDeptId() != null) {
                deptService.getRequiredActiveDepartment(user.getDeptId());
            }
            authorizationService.requireDelegableUserRoles(user);
        }
        user.setStatus(status);
        updateById(user);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 密码重置独立授权，写入后撤销所有旧登录凭证。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:resetPassword",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void resetPassword(Long userId, String password) {
        SysUser user = getRequiredUserInScope(userId);
        authorizationService.requireManageableUser(userId);
        user.setPassword(encodePassword(password));
        updateById(user);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 软删除账号并清理关联，禁止删除自己及最后一个有效管理员。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:delete",
            tables = @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void deleteUser(Long userId) {
        authorizationService.lockAdministratorMembership();
        SysUser user = getRequiredUserInScope(userId);
        authorizationService.requireManageableUser(userId);
        if (Objects.equals(userId, authenticationContext.getUserId())) {
            throw new DataAccessDeniedException("不能删除当前登录账号");
        }
        authorizationService.requireAdministratorCanBeRemoved(userId);
        if (baseMapper.hasOrganizationOwnershipReferences(userId)) {
            throw new IllegalArgumentException("用户仍为岗位或部门负责人，不能删除");
        }
        userRoleService.lambdaUpdate().eq(SysUserRole::getUserId, userId).remove();
        userPostService.lambdaUpdate().eq(SysUserPost::getUserId, userId).remove();
        user.setDeleted(1);
        if (!removeById(user)) {
            throw new DataAccessDeniedException("用户已不可删除");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 校验 BCrypt 实际字节上限后复用共享密码工具。 */
    private String encodePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("密码UTF-8编码不能超过72字节");
        }
        return BCryptUtils.encode(password);
    }

    /** 转换用户响应，严格排除密码字段。 */
    private UserVO toUserVO(SysUser user) {
        UserVO response = new UserVO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setDeptId(user.getDeptId());
        response.setStatus(user.getStatus());
        response.setRemark(user.getRemark());
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}

