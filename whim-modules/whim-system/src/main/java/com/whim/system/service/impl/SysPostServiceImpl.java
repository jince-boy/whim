package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.mapper.SysUserPostMapper;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.dto.post.PostSaveDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.post.PostVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserTenantService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统岗位表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final ISysUserTenantService userTenantService;
    private final ISysUserService userService;
    private final SysUserPostMapper userPostMapper;

    /** 按列表权限范围分页读取当前租户岗位，分页总数使用同一条件。 */
    @Override
    public PageDataVO<PostVO> pageCurrentTenantPosts(PageQueryDTO query) {
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize();
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("页码必须大于0，且每页数量必须在1到100之间");
        }
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:list");
        IPage<SysPost> posts = page(new Page<>(pageNum, pageSize),
                scopedQuery(scope).orderByAsc(SysPost::getSort, SysPost::getId));
        return new PageDataVO<>(posts.convert(this::toPostVO));
    }

    /** 按详情权限范围读取当前租户岗位。 */
    @Override
    public PostVO getCurrentTenantPost(Long postId) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:detail");
        return toPostVO(requireVisiblePost(postId, scope));
    }

    /** 按成员查看动作的数据范围读取岗位成员。 */
    @Override
    public Set<Long> getCurrentTenantPostMemberIds(Long postId) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:member:list");
        requireVisiblePost(postId, scope);
        Set<Long> userIds = new LinkedHashSet<>();
        for (SysUserPost binding : userPostMapper.selectList(Wrappers.<SysUserPost>lambdaQuery()
                .eq(SysUserPost::getTenantId, scope.getTenantId())
                .eq(SysUserPost::getPostId, postId).orderByAsc(SysUserPost::getUserId))) {
            userIds.add(binding.getUserId());
        }
        userTenantService.requireMembersInDataScope(userIds, scope);
        return userIds;
    }

    /** 按创建权限范围验证部门并创建当前租户岗位。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCurrentTenantPost(PostSaveDTO request) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:create");
        requireWritableDepartment(request.getDeptId(), scope);
        String postCode = request.getPostCode().trim();
        if (baseMapper.existsTenantPostCode(scope.getTenantId(), postCode, null)) {
            throw new IllegalArgumentException("当前租户已有相同岗位编码");
        }
        SysPost post = new SysPost();
        post.setDeptId(request.getDeptId());
        post.setPostName(request.getPostName().trim());
        post.setPostCode(postCode);
        post.setSort(request.getSort());
        post.setStatus(0);
        post.setRemark(request.getRemark());
        post.setCreateBy(scope.getUserId());
        try {
            save(post);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("当前租户已有相同岗位编码", exception);
        }
        return post.getId();
    }

    /** 原记录和新部门都必须处于本次更新权限范围内。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentTenantPost(Long postId, PostSaveDTO request) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:update");
        requireVisiblePost(postId, scope);
        requireWritableDepartment(request.getDeptId(), scope);
        String postCode = request.getPostCode().trim();
        if (baseMapper.existsTenantPostCode(scope.getTenantId(), postCode, postId)) {
            throw new IllegalArgumentException("当前租户已有相同岗位编码");
        }
        SysPost changes = new SysPost();
        changes.setPostName(request.getPostName().trim());
        changes.setPostCode(postCode);
        changes.setSort(request.getSort());
        LambdaUpdateWrapper<SysPost> target = scopedUpdate(postId, scope)
                .set(SysPost::getDeptId, request.getDeptId())
                .set(SysPost::getRemark, request.getRemark());
        try {
            if (!update(changes, target)) {
                throw new TenantAccessDeniedException("岗位已不可操作");
            }
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("当前租户已有相同岗位编码", exception);
        }
    }

    /** 按状态变更权限范围启用或停用岗位。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentTenantPostStatus(Long postId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:status");
        requireVisiblePost(postId, scope);
        SysPost changes = new SysPost();
        changes.setStatus(status);
        if (!update(changes, scopedUpdate(postId, scope))) {
            throw new TenantAccessDeniedException("岗位已不可操作");
        }
    }

    /** 校验目标岗位和全部成员后，以单个事务覆盖当前租户的岗位成员关系。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCurrentTenantPostMembers(Long postId, Set<Long> userIds) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:member:assign");
        requireVisiblePost(postId, scope);
        List<SysUserPost> existing = userPostMapper.selectList(Wrappers.<SysUserPost>lambdaQuery()
                .eq(SysUserPost::getTenantId, scope.getTenantId())
                .eq(SysUserPost::getPostId, postId));
        Set<Long> existingUserIds = new HashSet<>();
        existing.forEach(binding -> existingUserIds.add(binding.getUserId()));
        Set<Long> involvedUserIds = new HashSet<>(userIds);
        involvedUserIds.addAll(existingUserIds);
        List<SysUserTenant> members = userTenantService.lockMembersInDataScope(involvedUserIds, scope);
        if (!userIds.isEmpty()) {
            long activeMembers = members.stream().filter(member -> userIds.contains(member.getUserId())
                    && member.getStatus() == 0).count();
            if (activeMembers != userIds.size()) {
                throw new TenantAccessDeniedException("目标用户不是当前租户的有效成员");
            }
            long activeUsers = userService.lambdaQuery().in(SysUser::getId, userIds)
                    .eq(SysUser::getStatus, 0).count();
            if (activeUsers != userIds.size()) {
                throw new TenantAccessDeniedException("目标成员账号不可用");
            }
        }
        userPostMapper.removeExcludedMembers(postId, scope.getTenantId(), userIds, scope.getUserId());
        for (Long userId : userIds) {
            if (!existingUserIds.contains(userId)) {
                userPostMapper.upsertBinding(IdWorker.getId(), userId, postId, scope.getTenantId(),
                        scope.getUserId());
            }
        }
    }

    /** 按删除权限范围软删岗位，保留成员关系的岗位不得删除。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCurrentTenantPost(Long postId) {
        DataScopeDecisionDTO scope = roleService.resolveCurrentTenantDataScope("system:post:delete");
        requireVisiblePost(postId, scope);
        if (userPostMapper.exists(Wrappers.<SysUserPost>lambdaQuery()
                .eq(SysUserPost::getTenantId, scope.getTenantId()).eq(SysUserPost::getPostId, postId))) {
            throw new IllegalArgumentException("岗位仍关联成员，不能删除");
        }
        if (baseMapper.softDeleteScoped(postId, scope) != 1) {
            throw new TenantAccessDeniedException("岗位已不可操作");
        }
    }

    /** 在本次动作的租户和部门或本人范围内读取岗位。 */
    private SysPost requireVisiblePost(Long postId, DataScopeDecisionDTO scope) {
        SysPost post = getOne(scopedQuery(scope).eq(SysPost::getId, postId));
        if (post == null) {
            throw new TenantAccessDeniedException("岗位不存在或不在本次操作范围内");
        }
        return post;
    }

    /** 验证新岗位部门属于当前租户及本次写入范围。 */
    private void requireWritableDepartment(Long deptId, DataScopeDecisionDTO scope) {
        if (deptId != null) {
            deptService.getRequiredActiveDepartment(deptId, scope.getTenantId());
        }
        if (scope.isAll() || scope.getDeptIds().contains(deptId)) {
            return;
        }
        if (scope.isSelf() && (deptId == null || Objects.equals(deptId,
                userTenantService.getActiveDepartmentId(scope.getUserId(), scope.getTenantId())))) {
            return;
        }
        throw new TenantAccessDeniedException("目标部门不在本次操作的数据范围内");
    }

    /** 构建列表和详情共用的租户与数据范围查询条件。 */
    private LambdaQueryWrapper<SysPost> scopedQuery(DataScopeDecisionDTO scope) {
        LambdaQueryWrapper<SysPost> query = Wrappers.<SysPost>lambdaQuery()
                .eq(SysPost::getTenantId, scope.getTenantId());
        if (scope.isAll()) {
            return query;
        }
        if (scope.getDeptIds().isEmpty()) {
            return query.eq(SysPost::getCreateBy, scope.getUserId());
        }
        if (scope.isSelf()) {
            return query.and(condition -> condition.in(SysPost::getDeptId, scope.getDeptIds())
                    .or().eq(SysPost::getCreateBy, scope.getUserId()));
        }
        return query.in(SysPost::getDeptId, scope.getDeptIds());
    }

    /** 构建写操作共用的租户与数据范围目标条件。 */
    private LambdaUpdateWrapper<SysPost> scopedUpdate(Long postId, DataScopeDecisionDTO scope) {
        LambdaUpdateWrapper<SysPost> update = Wrappers.<SysPost>lambdaUpdate()
                .eq(SysPost::getId, postId).eq(SysPost::getTenantId, scope.getTenantId());
        if (scope.isAll()) {
            return update;
        }
        if (scope.getDeptIds().isEmpty()) {
            return update.eq(SysPost::getCreateBy, scope.getUserId());
        }
        if (scope.isSelf()) {
            return update.and(condition -> condition.in(SysPost::getDeptId, scope.getDeptIds())
                    .or().eq(SysPost::getCreateBy, scope.getUserId()));
        }
        return update.in(SysPost::getDeptId, scope.getDeptIds());
    }

    /** 将岗位实体转换为岗位接口响应。 */
    private PostVO toPostVO(SysPost post) {
        PostVO response = new PostVO();
        response.setId(post.getId());
        response.setDeptId(post.getDeptId());
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

