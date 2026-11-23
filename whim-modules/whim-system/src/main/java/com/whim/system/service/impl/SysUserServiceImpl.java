package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.constant.CacheKeys;
import com.whim.core.exception.AccessDeniedException;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BCryptUtils;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.file.support.FileUploadSecurity;
import com.whim.file.support.ImageThumbnailGenerator;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.redis.utils.CacheUtils;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.file.SysFileUploadDTO;
import com.whim.system.model.dto.user.SysUserAddDTO;
import com.whim.system.model.dto.user.SysUserAvatarQueryDTO;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.dto.user.SysUserPasswordDTO;
import com.whim.system.model.dto.user.SysUserQueryDTO;
import com.whim.system.model.dto.user.SysUserRoleDTO;
import com.whim.system.model.dto.user.SysUserUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysFile;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.entity.SysUserTwoFactor;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.model.vo.user.SysUserDetailVO;
import com.whim.system.model.vo.user.SysUserRoleOptionVO;
import com.whim.system.model.vo.user.SysUserRolesVO;
import com.whim.system.model.vo.user.SysUserVO;
import com.whim.system.service.ISysFileService;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
 * @description 用户资料、角色及头像管理。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private static final String AVATAR_UPLOAD_SOURCE = "userAvatar";
    private static final long MAXIMUM_AVATAR_BYTES = 5 * 1024 * 1024;
    private static final Set<String> AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif");
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final ISysPostService postService;
    private final ISysUserPostService userPostService;
    private final ISysRoleService roleService;
    private final ISysUserRoleService userRoleService;
    private final SysDeptMapper deptMapper;
    private final ISysFileService fileService;

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
                .select(SysUser::getId, SysUser::getUsername, SysUser::getSuperAdministrator, SysUser::getName, SysUser::getAvatar, SysUser::getDeptId,
                        SysUser::getEmail, SysUser::getPhone, SysUser::getGender, SysUser::getStatus,
                        SysUser::getRemark, SysUser::getCreateTime)
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getName()), SysUser::getName, query.getName())
                .like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .in(query.getDeptId() != null, SysUser::getDeptId, departmentIds)
                .isNull(Boolean.TRUE.equals(query.getUnassigned()), SysUser::getDeptId)
                .orderByDesc(SysUser::getCreateTime, SysUser::getId).page(query.getPage());
        Set<Long> visibleDepartmentIds = users.getRecords().stream().map(SysUser::getDeptId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 仅补齐本页已授权用户的部门名称；使用框架 Db 查询避免部门服务与用户服务循环注入。
        Map<Long, String> departmentNames = visibleDepartmentIds.isEmpty() ? Map.of()
                : Db.lambdaQuery(SysDept.class).select(SysDept::getId, SysDept::getDeptName)
                .in(SysDept::getId, visibleDepartmentIds).list().stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        // 仅超级管理员可恢复他人账号；按本页用户批量查询绑定标识，不读取密钥、恢复码或本人绑定。
        UserInfo operator = authenticationContext.getCurrentUserInfo();
        List<Long> recoveryTargets = operator.isSuperAdministrator()
                ? users.getRecords().stream().map(SysUser::getId)
                        .filter(id -> !id.equals(operator.getUserId())).toList()
                : List.of();
        Map<Long, Long> visibleBindings = recoveryTargets.isEmpty() ? Map.of()
                : Db.lambdaQuery(SysUserTwoFactor.class).select(SysUserTwoFactor::getUserId, SysUserTwoFactor::getId)
                .in(SysUserTwoFactor::getUserId, recoveryTargets).list().stream()
                .collect(Collectors.toMap(SysUserTwoFactor::getUserId, SysUserTwoFactor::getId));
        return new PageDataVO<>(users.convert(user -> {
            SysUserVO result = BeanConvertUtils.convert(user, SysUserVO.class);
            result.setDeptName(user.getDeptId() == null ? null : departmentNames.get(user.getDeptId()));
            result.setTwoFactorBindingId(visibleBindings.get(user.getId()));
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
     * 查询普通用户当前角色和可分配角色，超级管理员无需进行角色分配。
     *
     * @param id 用户ID
     * @return 当前角色及候选项
     */
    @Override
    @DataPermission(permission = "system:userRole:list", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public SysUserRolesVO getUserRoles(Long id) {
        SysUser user = this.getById(id);
        validateManagedUser(user);
        if (Boolean.TRUE.equals(user.getSuperAdministrator())) {
            throw new ServiceException("超级管理员默认拥有全部权限，无需分配角色");
        }
        SysUserRolesVO result = new SysUserRolesVO();
        result.setId(id);
        result.setRoleIds(userRoleService.lambdaQuery().select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, id).list().stream().map(SysUserRole::getRoleId).toList());
        boolean disabled = id.equals(authenticationContext.getUserId());
        // 仅返回分配操作需要的角色字段；已停用的现有角色保留回显并允许移除。
        for (SysRole role : roleService.lambdaQuery()
                .select(SysRole::getId, SysRole::getRoleName, SysRole::getRoleCode, SysRole::getStatus)
                .and(wrapper -> {
                    wrapper.eq(SysRole::getStatus, 0);
                    if (!result.getRoleIds().isEmpty()) {
                        wrapper.or().in(SysRole::getId, result.getRoleIds());
                    }
                })
                .orderByAsc(SysRole::getSort, SysRole::getId).list()) {
            SysUserRoleOptionVO option = new SysUserRoleOptionVO();
            option.setId(role.getId());
            option.setRoleName(role.getRoleName());
            option.setRoleCode(role.getRoleCode());
            option.setStatus(role.getStatus());
            option.setDisabled(disabled);
            result.getOptions().add(option);
        }
        return result;
    }

    /**
     * 读取头像缩略图或原文件，仅允许访问头像来源的图片。
     *
     * @param query 头像文件及预览方式
     * @param response 图片响应，原文件以附件返回
     * @throws IOException 写入缩略图响应失败
     */
    @Override
    public void getUserAvatar(SysUserAvatarQueryDTO query, HttpServletResponse response) throws IOException {
        boolean preview = Boolean.TRUE.equals(query.getPreview());
        // 头像读取不授予普通附件访问权限，原图同时遵守头像类型和大小限制。
        if (!fileService.lambdaQuery().eq(SysFile::getId, query.getId())
                .eq(SysFile::getUploadSource, AVATAR_UPLOAD_SOURCE)
                .in(preview, SysFile::getContentType, AVATAR_CONTENT_TYPES)
                .le(preview, SysFile::getFileSize, MAXIMUM_AVATAR_BYTES).exists()) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        if (preview) {
            fileService.downloadFile(query.getId(), response);
            return;
        }
        byte[] avatar = fileService.getFileThumbnail(query.getId());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (avatar.length == 0) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", FileUploadSecurity.CONTENT_SECURITY_POLICY);
        response.setContentLength(avatar.length);
        response.getOutputStream().write(avatar);
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
     * 校验本地图片并通过默认存储上传头像。
     *
     * @param parameter 头像上传参数
     * @return 用于保存用户资料的头像地址
     */
    @Override
    public String addUserAvatar(SysUserAvatarUploadDTO parameter) {
        MultipartFile file = parameter.getFile();
        if (file.isEmpty() || file.getSize() > MAXIMUM_AVATAR_BYTES) {
            throw new ServiceException("头像图片不能为空且不能超过5MB");
        }
        if (!AVATAR_CONTENT_TYPES.contains(Objects.requireNonNullElse(file.getContentType(), ""))) {
            throw new ServiceException("头像仅支持JPG、PNG或GIF图片");
        }
        // 实际解码成功才接收头像，不能仅凭扩展名或客户端声明判断图片有效。
        try {
            if (ImageThumbnailGenerator.generateThumbnail(file.getInputStream(), 96, 96).length == 0) {
                throw new ServiceException("头像图片无法识别、已损坏或像素过大，请更换图片");
            }
        } catch (IOException exception) {
            throw new ServiceException("读取头像图片失败，请重新选择图片");
        }
        SysFileUploadDTO upload = new SysFileUploadDTO();
        upload.setFile(file);
        upload.setUploadSource(AVATAR_UPLOAD_SOURCE);
        // 用户资料保存前只生成文件，取消或保存失败不会改变原头像。
        return "/system/user/avatar?id=" + fileService.addFile(upload).getId();
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
            // 普通账号不能通过调整自身组织归属扩大范围；超级管理员本身已具有全部数据权限。
            if (!authenticationContext.isSuperAdministrator()) {
                Set<Long> currentPostIds = userPostService.lambdaQuery().select(SysUserPost::getPostId)
                        .eq(SysUserPost::getUserId, user.getId())
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
     * 按角色分配操作权限和目标用户数据范围更新完整角色集合。
     *
     * @param parameter 用户ID及完整角色集合
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:userRole:assign", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public void assignUserRoles(SysUserRoleDTO parameter) {
        SysUser user = this.getById(parameter.getId());
        validateManagedUser(user);
        if (Boolean.TRUE.equals(user.getSuperAdministrator())) {
            throw new ServiceException("超级管理员默认拥有全部权限，无需分配角色");
        }
        if (parameter.getId().equals(authenticationContext.getUserId())) {
            throw new ServiceException("不能修改当前账号的角色");
        }
        Set<Long> requestedIds = new LinkedHashSet<>(parameter.getRoleIds());
        Set<Long> currentIds = userRoleService.lambdaQuery().select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, parameter.getId())
                .list().stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> addedIds = new LinkedHashSet<>(requestedIds);
        addedIds.removeAll(currentIds);
        Set<Long> removedIds = new LinkedHashSet<>(currentIds);
        removedIds.removeAll(requestedIds);
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
     * 应用迁移完成后失效上传途径字典缓存，使新增头像来源立即可用。
     */
    @EventListener(ApplicationStartedEvent.class)
    public void refreshUserAvatarUploadSourceCache() {
        // Flyway更新了已有字典，Redis中上次运行的旧选项不能阻止首次头像上传。
        CacheUtils.evict(CacheKeys.SYS_DICT, "sys_file_upload_source");
    }

    /**
     * 校验目标账号存在性及超级管理员账号的维护边界。
     *
     * @param user 已按本次操作数据范围查询的用户
     */
    private void validateManagedUser(SysUser user) {
        if (user == null) {
            throw new ServiceException("用户不存在或无权操作");
        }
        // 普通角色授权不能越过账号身份边界，防止重置密码或组织维护取得超级管理员身份。
        if (Boolean.TRUE.equals(user.getSuperAdministrator()) && !authenticationContext.isSuperAdministrator()) {
            throw new AccessDeniedException("不能维护超级管理员账号");
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
        Set<Long> currentIds = userPostService.lambdaQuery().select(SysUserPost::getPostId)
                .eq(SysUserPost::getUserId, userId)
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
