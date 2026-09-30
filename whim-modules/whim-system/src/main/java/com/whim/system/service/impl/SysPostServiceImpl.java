package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionScope;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.model.dto.post.PostSaveDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.vo.post.PostVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 岗位任职管理，岗位不隐含角色授权，负责人独立于创建审计。
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {
    private final ISysDeptService deptService;
    private final ISysUserService userService;
    private final ISysUserPostService userPostService;

    /** 同一操作范围过滤分页数据和分页总数。 */
    @Override
    @DataPermission(permission = "system:post:list", tables = @DataPermissionTable(name = "sys_post"))
    public PageDataVO<PostVO> pagePosts(PageQueryDTO query) {
        return new PageDataVO<>(page(new Page<>(query.getPageNum(), query.getPageSize()),
                lambdaQuery().orderByAsc(SysPost::getSort, SysPost::getId).getWrapper()).convert(this::toPostVO));
    }

    /** 详情查询仍经过数据权限拦截，不能仅凭ID绕过列表范围。 */
    @Override
    @DataPermission(permission = "system:post:detail", tables = @DataPermissionTable(name = "sys_post"))
    public PostVO getPost(Long postId) {
        return toPostVO(requiredPost(postId, false));
    }

    /** 先校验岗位，再过滤成员账号，避免泄露不可见用户ID。 */
    @Override
    @DataPermission(permission = "system:post:member:list", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public Set<Long> getPostMemberIds(Long postId) {
        requiredPost(postId, false);
        Set<Long> userIds = userPostService.lambdaQuery().eq(SysUserPost::getPostId, postId).list().stream()
                .map(SysUserPost::getUserId).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Set.of();
        }
        return userService.lambdaQuery().select(SysUser::getId).in(SysUser::getId, userIds).list().stream()
                .map(SysUser::getId).collect(Collectors.toSet());
    }

    /** 校验归属后创建岗位，实际操作者作为初始业务负责人。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:create", tables = @DataPermissionTable(name = "sys_post"))
    public Long createPost(PostSaveDTO request) {
        DataPermissionScope scope = DataPermissionContext.requiredScope();
        requireWritableDepartment(request.getDeptId(), scope.getUserId(), scope);
        SysPost post = new SysPost();
        post.setPostName(request.getPostName().trim());
        post.setPostCode(request.getPostCode().trim());
        post.setDeptId(request.getDeptId());
        post.setOwnerUserId(scope.getUserId());
        post.setSort(request.getSort());
        post.setStatus(0);
        post.setRemark(request.getRemark());
        save(post);
        return post.getId();
    }

    /** 同时校验原岗位和新部门，不允许通过变更归属越权写入。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:update", tables = @DataPermissionTable(name = "sys_post"))
    public void updatePost(Long postId, PostSaveDTO request) {
        SysPost post = requiredPost(postId, true);
        requireWritableDepartment(request.getDeptId(), post.getOwnerUserId(), DataPermissionContext.requiredScope());
        SysPost changes = new SysPost();
        changes.setPostName(request.getPostName().trim());
        changes.setPostCode(request.getPostCode().trim());
        changes.setSort(request.getSort());
        if (!update(changes, lambdaUpdate().eq(SysPost::getId, postId)
                .set(SysPost::getDeptId, request.getDeptId()).set(SysPost::getRemark, request.getRemark())
                .getWrapper())) {
            throw new DataAccessDeniedException("岗位已不可修改");
        }
    }

    /** 启停本次操作可见岗位。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:status", tables = @DataPermissionTable(name = "sys_post"))
    public void setPostStatus(Long postId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysPost post = requiredPost(postId, true);
        post.setStatus(status);
        updateById(post);
    }

    /** 全部新旧成员校验通过后替换岗位关联，岗位不会改变账号的角色或主部门。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:member:assign", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public void replacePostMembers(Long postId, Set<Long> userIds) {
        SysPost post = requiredPost(postId, true);
        if (post.getStatus() != 0 && !userIds.isEmpty()) {
            throw new IllegalArgumentException("不能为停用岗位分配用户");
        }
        List<SysUserPost> existing = userPostService.lambdaQuery().eq(SysUserPost::getPostId, postId).list();
        Set<Long> existingIds = existing.stream().map(SysUserPost::getUserId).collect(Collectors.toSet());
        Set<Long> involved = new HashSet<>(existingIds);
        involved.addAll(userIds);
        List<SysUser> users = userService.requireUsersInScope(involved, false, true);
        if (users.stream().anyMatch(user -> userIds.contains(user.getId()) && user.getStatus() != 0)) {
            throw new DataAccessDeniedException("不能为停用用户分配岗位");
        }
        var removals = userPostService.lambdaUpdate().eq(SysUserPost::getPostId, postId);
        if (!userIds.isEmpty()) {
            removals.notIn(SysUserPost::getUserId, userIds);
        }
        removals.remove();
        List<SysUserPost> additions = userIds.stream().filter(id -> !existingIds.contains(id)).map(userId -> {
            SysUserPost binding = new SysUserPost();
            binding.setUserId(userId);
            binding.setPostId(postId);
            return binding;
        }).toList();
        if (!additions.isEmpty()) {
            userPostService.saveBatch(additions);
        }
    }

    /** 删除没有用户关联的可见岗位。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:delete", tables = @DataPermissionTable(name = "sys_post"))
    public void deletePost(Long postId) {
        SysPost post = requiredPost(postId, true);
        if (userPostService.lambdaQuery().eq(SysUserPost::getPostId, postId).exists()) {
            throw new IllegalArgumentException("岗位仍关联用户，不能删除");
        }
        post.setDeleted(1);
        if (!removeById(post)) {
            throw new DataAccessDeniedException("岗位已不可删除");
        }
    }

    /** 查询可见岗位，写操作在事务中锁定目标记录。 */
    private SysPost requiredPost(Long postId, boolean lock) {
        var query = lambdaQuery().eq(SysPost::getId, postId);
        if (lock) {
            query.last("FOR UPDATE");
        }
        SysPost post = query.one();
        if (post == null) {
            throw new DataAccessDeniedException("岗位不存在或不在本次操作范围内");
        }
        return post;
    }

    /** 校验目标部门；纯本人范围只能将本人岗位归属到主部门或保留无部门。 */
    private void requireWritableDepartment(Long deptId, Long ownerUserId, DataPermissionScope scope) {
        DataPermissionContext.checkOwnership("sys_post", ownerUserId, deptId);
        if (deptId != null) {
            deptService.getRequiredActiveDepartment(deptId);
        }
        if (scope.isAll() || scope.getDepartmentIds().contains(deptId)
                || (scope.isSelf() && Objects.equals(ownerUserId, scope.getUserId())
                && (deptId == null || Objects.equals(deptId, scope.getUserDepartmentId())))) {
            return;
        }
        throw new DataAccessDeniedException("目标部门不在本次岗位写入范围内");
    }

    /** 转换岗位响应，业务负责人和创建审计分别返回。 */
    private PostVO toPostVO(SysPost post) {
        PostVO response = new PostVO();
        response.setId(post.getId());
        response.setDeptId(post.getDeptId());
        response.setOwnerUserId(post.getOwnerUserId());
        response.setPostName(post.getPostName());
        response.setPostCode(post.getPostCode());
        response.setSort(post.getSort());
        response.setStatus(post.getStatus());
        response.setRemark(post.getRemark());
        response.setCreateBy(post.getCreateBy());
        response.setCreateTime(post.getCreateTime());
        return response;
    }
}

