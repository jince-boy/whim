package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.user.SysUserAddDTO;
import com.whim.system.model.dto.user.SysUserPasswordDTO;
import com.whim.system.model.dto.user.SysUserQueryDTO;
import com.whim.system.model.dto.user.SysUserRoleDTO;
import com.whim.system.model.dto.user.SysUserUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.model.vo.user.SysUserDetailVO;
import com.whim.system.model.vo.user.SysUserRoleOptionVO;
import com.whim.system.model.vo.user.SysUserRolesVO;
import com.whim.system.model.vo.user.SysUserVO;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户管理及账号信息查询。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final ISysPostService postService;
    private final ISysUserPostService userPostService;
    private final ISysRoleService roleService;
    private final ISysUserRoleService userRoleService;
    private final SysDeptMapper deptMapper;

    /**
     * 按部门分支和用户信息分页查询可见用户。
     *
     * @param query 用户筛选及分页条件
     * @return 用户分页
     */
    @Override
    @DataPermission(permission = "system:user:list", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public PageDataVO<SysUserVO> getUserPage(SysUserQueryDTO query) {
        List<Long> departmentIds = query.getDeptId() == null
                ? List.of() : deptMapper.getDeptAndDescendantIds(query.getDeptId());
        if (query.getDeptId() != null && departmentIds.isEmpty()) {
            return new PageDataVO<>(query.getPage());
        }
        // 列表只查询展示字段，不读取密码哈希；部门筛选不会扩大当前操作的数据范围。
        Page<SysUser> users = this.lambdaQuery()
                .select(SysUser::getId, SysUser::getUsername, SysUser::getName, SysUser::getAvatar, SysUser::getDeptId,
                        SysUser::getEmail, SysUser::getPhone, SysUser::getGender, SysUser::getStatus,
                        SysUser::getRemark, SysUser::getCreateTime)
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getName()), SysUser::getName, query.getName())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .in(query.getDeptId() != null, SysUser::getDeptId, departmentIds)
                .isNull(Boolean.TRUE.equals(query.getUnassigned()), SysUser::getDeptId)
                .orderByDesc(SysUser::getCreateTime, SysUser::getId).page(query.getPage());
        Set<Long> userIds = users.getRecords().stream().map(SysUser::getId).collect(Collectors.toSet());
        Set<Long> superAdministratorIds = getSuperAdministratorIds(userIds);
        Set<Long> visibleDepartmentIds = users.getRecords().stream().map(SysUser::getDeptId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 仅补齐本页已授权用户的部门名称；使用框架 Db 查询避免部门服务与用户服务循环注入。
        Map<Long, String> departmentNames = visibleDepartmentIds.isEmpty() ? Map.of()
                : Db.lambdaQuery(SysDept.class).select(SysDept::getId, SysDept::getDeptName)
                .in(SysDept::getId, visibleDepartmentIds).list().stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        return new PageDataVO<>(users.convert(user -> {
            SysUserVO result = BeanConvertUtils.convert(user, SysUserVO.class);
            result.setDeptName(user.getDeptId() == null ? null : departmentNames.get(user.getDeptId()));
            result.setSuperAdministrator(superAdministratorIds.contains(user.getId()));
            return result;
        }));
    }

    /**
     * 查询用户编辑详情和当前岗位。
     *
     * @param id 用户ID
     * @return 用户编辑详情
     */
    @Override
    @DataPermission(permission = "system:user:detail", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public SysUserDetailVO getUserDetail(Long id) {
        SysUser user = this.getById(id);
        validateManagedUser(user);
        SysUserDetailVO result = BeanConvertUtils.convert(user, SysUserDetailVO.class);
        result.setSuperAdministrator(!getSuperAdministratorIds(Set.of(id)).isEmpty());
        if (user.getDeptId() != null) {
            SysDept department = Db.lambdaQuery(SysDept.class).select(SysDept::getId, SysDept::getDeptName)
                    .eq(SysDept::getId, user.getDeptId()).one();
            result.setDeptName(department == null ? null : department.getDeptName());
        }
        List<Long> postIds = userPostService.lambdaQuery().select(SysUserPost::getPostId)
                .eq(SysUserPost::getUserId, id).list().stream().map(SysUserPost::getPostId).toList();
        if (!postIds.isEmpty()) {
            // 编辑回显已有任职，包括停用岗位；新增任职仍由保存流程重新校验。
            result.setPosts(BeanConvertUtils.convertList(postService.lambdaQuery()
                    .in(SysPost::getId, postIds).orderByAsc(SysPost::getSort, SysPost::getId).list(), SysPostVO.class));
        }
        return result;
    }

    /**
     * 查询用户当前角色和可分配的角色。
     *
     * @param id 用户ID
     * @return 当前角色及候选项
     */
    @Override
    @DataPermission(permission = "system:userRole:list", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public SysUserRolesVO getUserRoles(Long id) {
        validateManagedUser(this.getById(id));
        SysUserRolesVO result = new SysUserRolesVO();
        result.setId(id);
        result.setRoleIds(userRoleService.lambdaQuery().select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, id).list().stream().map(SysUserRole::getRoleId).toList());
        Set<Long> assignableIds = authenticationContext.getCurrentUserInfo().getRoleInfoList().stream()
                .map(RoleInfo::getRoleId).collect(Collectors.toSet());
        boolean administrator = authenticationContext.isSuperAdministrator();
        Set<Long> visibleRoleIds = new LinkedHashSet<>(result.getRoleIds());
        visibleRoleIds.addAll(assignableIds);
        if (!administrator && visibleRoleIds.isEmpty()) {
            return result;
        }
        // 普通管理员只读取已分配和自身持有的角色，不加载整个角色表再裁剪。
        for (SysRole role : roleService.lambdaQuery().in(!administrator, SysRole::getId, visibleRoleIds)
                .orderByAsc(SysRole::getSort, SysRole::getId).list()) {
            boolean assigned = result.getRoleIds().contains(role.getId());
            boolean assignable = administrator || (assignableIds.contains(role.getId())
                    && !"superadmin".equals(role.getRoleCode()));
            if (!assigned && (!assignable || role.getStatus() != 0)) {
                continue;
            }
            SysUserRoleOptionVO option = new SysUserRoleOptionVO();
            option.setId(role.getId());
            option.setRoleName(role.getRoleName());
            option.setRoleCode(role.getRoleCode());
            option.setStatus(role.getStatus());
            // 无权调整的已有角色保持只读，不能因候选列表裁剪而被意外移除。
            option.setDisabled(!assignable || id.equals(authenticationContext.getUserId()));
            result.getOptions().add(option);
        }
        return result;
    }

    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public SysUser getUserById(Long id) {
        return this.lambdaQuery()
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                .eq(SysUser::getId, id)
                .one();
    }

    /**
     * 按忽略大小写的用户名查询账号，登录阶段不依赖业务数据权限。
     *
     * @param username 用户名
     * @return 用户账号，不存在时返回 null
     */
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username.toLowerCase(Locale.ROOT)).one();
    }

    /**
     * 查询部门移动或启停影响的主部门用户、岗位成员和自定义数据范围角色用户。
     *
     * @param departmentIds 发生变化的非空部门ID集合
     * @return 需要重新加载部门授权信息的用户ID集合
     */
    @Override
    public Set<Long> getAffectedUserIdsByDepartments(Set<Long> departmentIds) {
        // 会话失效必须覆盖所有受影响用户，Mapper 的内部只读查询不受操作者可见范围限制。
        return baseMapper.getAffectedUserIdsByDepartments(departmentIds);
    }

    /**
     * 内部查询祖先部门中拥有本部门及下级范围角色的用户，用于组织变更后的会话失效。
     *
     * @param ancestorIds 受影响的祖先部门ID集合，不包含根标识0
     * @return 主部门位于指定祖先中且拥有启用的下级范围角色的未删除用户ID集合
     */
    @Override
    public Set<Long> getUserIdsByDepartmentAncestors(Set<Long> ancestorIds) {
        if (ancestorIds.isEmpty()) {
            return Set.of();
        }
        // 会话失效要覆盖所有受影响用户，不能被操作者的可见范围截断；这里仅执行内部只读查询。
        return DataPermissionContext.getMetadata(() -> {
            List<Long> userIds = this.lambdaQuery().select(SysUser::getId)
                    .in(SysUser::getDeptId, ancestorIds).list().stream().map(SysUser::getId).toList();
            if (userIds.isEmpty()) {
                return Set.of();
            }
            List<SysUserRole> userRoles = userRoleService.lambdaQuery()
                    .select(SysUserRole::getUserId, SysUserRole::getRoleId)
                    .in(SysUserRole::getUserId, userIds).list();
            if (userRoles.isEmpty()) {
                return Set.of();
            }
            // 批量筛选这些用户实际持有的角色；一人有多个符合条件的角色时，只使其会话失效一次。
            Set<Long> roleIds = roleService.lambdaQuery().select(SysRole::getId)
                    .in(SysRole::getId, userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet()))
                    .eq(SysRole::getStatus, 0)
                    .eq(SysRole::getDataScope, DataScopeType.DEPARTMENT_AND_DESCENDANTS.getCode())
                    .list().stream().map(SysRole::getId).collect(Collectors.toSet());
            return userRoles.stream().filter(relation -> roleIds.contains(relation.getRoleId()))
                    .map(SysUserRole::getUserId).collect(Collectors.toSet());
        });
    }

    /**
     * 新增用户并保存岗位任职关系。
     *
     * @param parameter 用户新增参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:create", tables = {
            @DataPermissionTable(name = "sys_user", userColumn = "id"),
            @DataPermissionTable(name = "sys_post")})
    public void addUser(SysUserAddDTO parameter) {
        SysUser user = BeanConvertUtils.convert(parameter, SysUser.class);
        user.setUsername(user.getUsername().toLowerCase(Locale.ROOT));
        user.setPassword(BCryptUtils.encode(parameter.getPassword()));
        // 新账号还没有本人身份，“本人”范围不能用于替其他人创建账号。
        DataPermissionContext.checkOwnership("sys_user", null, user.getDeptId());
        validateUserDepartment(user);
        if (!this.save(user)) {
            throw new ServiceException("新增用户失败");
        }
        updateUserPosts(user.getId(), new LinkedHashSet<>(parameter.getPostIds()));
    }

    /**
     * 修改用户及岗位，提交后失效登录会话。
     *
     * @param parameter 用户ID及完整修改字段
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:update", tables = {
            @DataPermissionTable(name = "sys_user", userColumn = "id"),
            @DataPermissionTable(name = "sys_post")})
    public void updateUser(SysUserUpdateDTO parameter) {
        SysUser originalUser = this.getById(parameter.getId());
        validateManagedUser(originalUser);
        SysUser user = BeanConvertUtils.convert(parameter, SysUser.class);
        boolean departmentChanged = !Objects.equals(originalUser.getDeptId(), user.getDeptId());
        Set<Long> postIds = new LinkedHashSet<>(parameter.getPostIds());
        if (user.getId().equals(authenticationContext.getUserId())) {
            if (user.getStatus() != 0) {
                throw new ServiceException("不能停用当前登录账号");
            }
            // 内置 admin 只能由本人维护组织归属，其他账号仍不能自行扩张组织身份。
            if (!"admin".equals(originalUser.getUsername())) {
                Set<Long> currentPostIds = userPostService.lambdaQuery().eq(SysUserPost::getUserId, user.getId())
                        .list().stream().map(SysUserPost::getPostId).collect(Collectors.toSet());
                if (departmentChanged || !currentPostIds.equals(postIds)) {
                    throw new ServiceException("不能调整自己的部门、岗位");
                }
            }
        }
        DataPermissionContext.checkOwnership("sys_user", user.getId(), user.getDeptId());
        validateUserDepartment(user);
        if (departmentChanged || user.getStatus() != 0) {
            validateUserLeadership(user.getId());
        }
        if (!this.updateById(user)) {
            throw new ServiceException("修改用户失败");
        }
        updateUserPosts(user.getId(), postIds);
        // 头像、姓名、部门、岗位及状态都进入登录快照；提交后统一失效，回滚不影响登录。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(user.getId()));
    }

    /**
     * 修改用户密码并在事务提交后使其全部会话失效。
     *
     * @param parameter 用户ID及新密码
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:resetPassword", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void resetUserPassword(SysUserPasswordDTO parameter) {
        SysUser user = this.getById(parameter.getId());
        validateManagedUser(user);
        // 安全校验已读取完整账号，直接修改其密码，保留所有允许清空的资料字段。
        user.setPassword(BCryptUtils.encode(parameter.getPassword()));
        if (!this.updateById(user)) {
            throw new ServiceException("修改密码失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(user.getId()));
    }

    /**
     * 校验角色可授予范围后更新用户角色。
     *
     * @param parameter 用户ID及完整角色集合
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:userRole:assign", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void assignUserRoles(SysUserRoleDTO parameter) {
        validateManagedUser(this.getById(parameter.getId()));
        if (parameter.getId().equals(authenticationContext.getUserId())) {
            throw new ServiceException("不能修改当前账号的角色");
        }
        Set<Long> requestedIds = new LinkedHashSet<>(parameter.getRoleIds());
        Set<Long> currentIds = userRoleService.lambdaQuery().eq(SysUserRole::getUserId, parameter.getId())
                .list().stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> addedIds = new LinkedHashSet<>(requestedIds);
        addedIds.removeAll(currentIds);
        Set<Long> removedIds = new LinkedHashSet<>(currentIds);
        removedIds.removeAll(requestedIds);
        if (!authenticationContext.isSuperAdministrator()) {
            // 普通管理员只能调整自己已持有的角色，不能借分配角色取得更高授权。
            Set<Long> assignableIds = authenticationContext.getCurrentUserInfo().getRoleInfoList().stream()
                    .filter(role -> !"superadmin".equals(role.getRoleCode()))
                    .map(RoleInfo::getRoleId).collect(Collectors.toSet());
            if (!assignableIds.containsAll(addedIds) || !assignableIds.containsAll(removedIds)) {
                throw new DataAccessDeniedException("不能分配或移除超出自身授权的角色");
            }
        }
        if (!addedIds.isEmpty()) {
            long availableCount = roleService.lambdaQuery().in(SysRole::getId, addedIds)
                    .eq(SysRole::getStatus, 0).count();
            if (availableCount != addedIds.size()) {
                throw new ServiceException("新增分配的角色不存在或已停用");
            }
        }
        if (addedIds.isEmpty() && removedIds.isEmpty()) {
            return;
        }
        if (!removedIds.isEmpty()) {
            userRoleService.lambdaUpdate().eq(SysUserRole::getUserId, parameter.getId())
                    .in(SysUserRole::getRoleId, removedIds).remove();
        }
        if (!addedIds.isEmpty()) {
            List<SysUserRole> relations = addedIds.stream().map(roleId -> {
                SysUserRole relation = new SysUserRole();
                relation.setUserId(parameter.getId());
                relation.setRoleId(roleId);
                return relation;
            }).toList();
            if (!userRoleService.saveBatch(relations)) {
                throw new ServiceException("分配角色失败");
            }
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(parameter.getId()));
    }

    /**
     * 删除用户及关联关系，禁止删除当前账号或仍担任部门负责人的用户。
     *
     * @param id 用户ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:user:delete", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void deleteUser(Long id) {
        validateManagedUser(this.getById(id));
        if (id.equals(authenticationContext.getUserId())) {
            throw new ServiceException("不能删除当前登录账号");
        }
        validateUserLeadership(id);
        if (!this.removeById(id)) {
            throw new ServiceException("删除用户失败");
        }
        // 用户软删除、任职和角色关系解除必须原子完成，用户名仍保留全局唯一性。
        userPostService.lambdaUpdate().eq(SysUserPost::getUserId, id).remove();
        userRoleService.lambdaUpdate().eq(SysUserRole::getUserId, id).remove();
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(id));
    }

    /**
     * 批量识别指定账号中的超级管理员，避免列表逐行查询角色。
     *
     * @param userIds 用户ID集合
     * @return 持有超级管理员角色的用户ID集合
     */
    private Set<Long> getSuperAdministratorIds(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Set.of();
        }
        SysRole role = roleService.lambdaQuery().eq(SysRole::getRoleCode, "superadmin").one();
        if (role == null) {
            return Set.of();
        }
        return userRoleService.lambdaQuery().select(SysUserRole::getUserId)
                .eq(SysUserRole::getRoleId, role.getId()).in(SysUserRole::getUserId, userIds)
                .list().stream().map(SysUserRole::getUserId).collect(Collectors.toSet());
    }

    /**
     * 校验目标账号存在、内置管理员仅限本人维护及超级管理员保护规则。
     *
     * @param user 已按本次操作数据范围查询的用户
     */
    private void validateManagedUser(SysUser user) {
        if (user == null) {
            throw new ServiceException("用户不存在或无权操作");
        }
        // admin 的用户名不可修改；即使操作者同为超级管理员，也不能代为维护该账号。
        if ("admin".equals(user.getUsername()) && !user.getId().equals(authenticationContext.getUserId())) {
            throw new DataAccessDeniedException("admin账号仅允许本人维护");
        }
        if (!authenticationContext.isSuperAdministrator()
                && !getSuperAdministratorIds(Set.of(user.getId())).isEmpty()) {
            throw new DataAccessDeniedException("不能操作超级管理员账号");
        }
    }

    /**
     * 校验主部门归属，启用用户只能选择有效的部门分支。
     *
     * @param user 待保存的用户
     */
    private void validateUserDepartment(SysUser user) {
        if (user.getDeptId() == null) {
            return;
        }
        Integer status = deptMapper.getDeptEffectiveStatusById(user.getDeptId());
        if (status == null) {
            throw new ServiceException("所属部门不存在");
        }
        if (user.getStatus() == 0 && status != 0) {
            throw new ServiceException("启用用户的所属部门及上级部门必须启用");
        }
    }

    /**
     * 检查用户是否仍担任部门负责人，避免组织归属变更后留下失效关系。
     *
     * @param id 用户ID
     */
    private void validateUserLeadership(Long id) {
        // 完整性检查涵盖停用部门；与部门编辑共用组织锁，不自动清空操作者无权修改的部门。
        if (Db.lambdaQuery(SysDept.class).eq(SysDept::getLeaderUserId, id).exists()) {
            throw new ServiceException("用户仍担任部门负责人，请先在部门管理中调整负责人");
        }
    }

    /**
     * 按差集维护用户岗位，只对新增任职校验有效状态和当前操作的数据权限。
     *
     * @param userId 用户ID
     * @param requestedIds 完整岗位ID集合
     */
    private void updateUserPosts(Long userId, Set<Long> requestedIds) {
        Set<Long> currentIds = userPostService.lambdaQuery().eq(SysUserPost::getUserId, userId)
                .list().stream().map(SysUserPost::getPostId).collect(Collectors.toSet());
        Set<Long> addedIds = new LinkedHashSet<>(requestedIds);
        addedIds.removeAll(currentIds);
        Set<Long> removedIds = new LinkedHashSet<>(currentIds);
        removedIds.removeAll(requestedIds);
        if (!addedIds.isEmpty()) {
            List<SysPost> posts = postService.lambdaQuery().in(SysPost::getId, addedIds)
                    .eq(SysPost::getStatus, 0).list();
            if (posts.size() != addedIds.size()) {
                throw new ServiceException("新增岗位不存在、已停用或超出授权范围");
            }
            Set<Long> departmentIds = posts.stream().map(SysPost::getDeptId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            if (!departmentIds.isEmpty()
                    && deptMapper.getValidDepartmentsByIds(departmentIds).size() != departmentIds.size()) {
                throw new ServiceException("新增岗位的所属部门或上级部门已停用");
            }
        }
        if (!removedIds.isEmpty()) {
            userPostService.lambdaUpdate().eq(SysUserPost::getUserId, userId)
                    .in(SysUserPost::getPostId, removedIds).remove();
        }
        if (!addedIds.isEmpty()) {
            List<SysUserPost> relations = addedIds.stream().map(postId -> {
                SysUserPost relation = new SysUserPost();
                relation.setUserId(userId);
                relation.setPostId(postId);
                return relation;
            }).toList();
            if (!userPostService.saveBatch(relations)) {
                throw new ServiceException("保存用户岗位失败");
            }
        }
    }
}
