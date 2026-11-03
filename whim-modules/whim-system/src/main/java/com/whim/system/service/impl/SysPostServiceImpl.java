package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.model.dto.post.SysPostAddDTO;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.dto.post.SysPostMembersDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostStatusDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位管理与成员分配，岗位只表达任职关系，不授予角色。
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final SysDeptMapper deptMapper;
    private final ISysUserService userService;
    private final ISysUserPostService userPostService;

    /**
     * 按名称、编码、部门和状态分页查询可见岗位。
     *
     * @param query 岗位筛选及分页条件
     * @return 按排序值和ID升序排列的岗位分页
     */
    @Override
    @DataPermission(permission = "system:post:list", tables =
            @DataPermissionTable(name = "sys_post"))
    public PageDataVO<SysPostVO> getPostPage(SysPostQueryDTO query) {
        Page<SysPost> posts = this.lambdaQuery()
                .like(StringUtils.hasText(query.getPostName()), SysPost::getPostName, query.getPostName())
                .like(StringUtils.hasText(query.getPostCode()), SysPost::getPostCode, query.getPostCode())
                .eq(query.getDeptId() != null, SysPost::getDeptId, query.getDeptId())
                .isNull(Boolean.TRUE.equals(query.getOrganizationWide()), SysPost::getDeptId)
                .eq(query.getStatus() != null, SysPost::getStatus, query.getStatus())
                .orderByAsc(SysPost::getSort, SysPost::getId)
                .page(query.getPage());
        return new PageDataVO<>(posts.convert(post -> BeanConvertUtils.convert(post, SysPostVO.class)));
    }

    /**
     * 分页查询岗位已分配成员或未分配的正常用户。
     *
     * @param query 岗位ID、成员类型及用户筛选条件
     * @return 岗位和用户均在本次操作范围内的用户分页
     */
    @Override
    @DataPermission(permission = "system:post:member:list", tables =
            @DataPermissionTable(name = "sys_post"))
    public PageDataVO<SysPostMemberVO> getPostMemberPage(SysPostMemberQueryDTO query) {
        if (!this.lambdaQuery().eq(SysPost::getId, query.getPostId()).exists()) {
            throw new ServiceException("岗位不存在或无权查看成员");
        }
        // 岗位访问与用户访问分别校验，不能因可见岗位而读取范围外用户。
        return userService.getPostMemberPage(query);
    }

    /**
     * 查询详情权限范围内的岗位。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    @Override
    @DataPermission(permission = "system:post:detail", tables =
            @DataPermissionTable(name = "sys_post"))
    public SysPostVO getPostDetail(Long id) {
        SysPost post = this.getById(id);
        if (post == null) {
            throw new ServiceException("岗位不存在或无权查看");
        }
        return BeanConvertUtils.convert(post, SysPostVO.class);
    }

    /**
     * 新增岗位并校验部门归属。
     *
     * @param parameter 岗位新增参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:create", tables =
            @DataPermissionTable(name = "sys_post"))
    public void addPost(SysPostAddDTO parameter) {
        SysPost post = BeanConvertUtils.convert(parameter, SysPost.class);
        // “本人”范围使用实际创建人；审计字段仍由框架填充，不从请求接收。
        DataPermissionContext.checkOwnership("sys_post", authenticationContext.getUserId(), post.getDeptId());
        validatePostDepartment(post);
        if (!this.save(post)) {
            throw new ServiceException("新增岗位失败");
        }
    }

    /**
     * 增量添加指定用户，已有成员不重复添加。
     *
     * @param parameter 岗位ID和本次选择的用户ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:member:assign", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public void addPostMembers(SysPostMembersDTO parameter) {
        SysPost post = this.getById(parameter.getPostId());
        if (post == null) {
            throw new ServiceException("岗位不存在或无权配置成员");
        }
        if (post.getStatus() != 0) {
            throw new ServiceException("停用岗位不能添加成员");
        }
        validatePostDepartment(post);
        Set<Long> userIds = new HashSet<>(parameter.getUserIds());
        // 全部用户必须正常且在本次配置范围内，任一不符合时整批拒绝。
        if (userService.lambdaQuery().in(SysUser::getId, userIds)
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode()).count() != userIds.size()) {
            throw new ServiceException("所选用户不存在、已停用或超出成员配置权限");
        }
        List<Long> assignedUserIds = userPostService.lambdaQuery().select(SysUserPost::getUserId)
                .eq(SysUserPost::getPostId, post.getId()).in(SysUserPost::getUserId, userIds)
                .list().stream().map(SysUserPost::getUserId).toList();
        userIds.removeAll(assignedUserIds);
        if (userIds.isEmpty()) {
            return;
        }
        List<SysUserPost> relations = userIds.stream().map(userId -> {
            SysUserPost relation = new SysUserPost();
            relation.setPostId(post.getId());
            relation.setUserId(userId);
            return relation;
        }).toList();
        if (!userPostService.saveBatch(relations)) {
            throw new ServiceException("添加岗位成员失败");
        }
        // 仅实际新增的成员需要重建任职信息，回滚时保留原会话。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
    }

    /**
     * 修改岗位，提交后使任职信息变化的成员会话失效。
     *
     * @param parameter 岗位ID及完整修改字段
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:update", tables =
            @DataPermissionTable(name = "sys_post"))
    public void updatePost(SysPostUpdateDTO parameter) {
        SysPost originalPost = this.getById(parameter.getId());
        if (originalPost == null) {
            throw new ServiceException("岗位不存在或无权修改");
        }
        SysPost post = BeanConvertUtils.convert(parameter, SysPost.class);
        // 变更所属部门后仍须在授权范围内，创建人保持原值且不能由请求修改。
        DataPermissionContext.checkOwnership("sys_post", originalPost.getCreateBy(), post.getDeptId());
        validatePostDepartment(post);
        if (!this.updateById(post)) {
            throw new ServiceException("修改岗位失败");
        }
        // 登录信息只缓存编码、名称及所属部门；排序和备注不影响任职或授权。
        if (!originalPost.getPostCode().equals(post.getPostCode())
                || !originalPost.getPostName().equals(post.getPostName())
                || !Objects.equals(originalPost.getDeptId(), post.getDeptId())
                || !originalPost.getStatus().equals(post.getStatus())) {
            invalidatePostSessions(post.getId());
        }
    }

    /**
     * 修改岗位状态，保留其他字段并在提交后使成员会话失效。
     *
     * @param parameter 岗位ID和目标状态
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:status", tables =
            @DataPermissionTable(name = "sys_post"))
    public void updatePostStatus(SysPostStatusDTO parameter) {
        SysPost post = this.getById(parameter.getId());
        if (post == null) {
            throw new ServiceException("岗位不存在或无权修改状态");
        }
        if (post.getStatus().equals(parameter.getStatus())) {
            return;
        }
        // 部门和备注允许清空，使用已读取的实体保留它们；审计字段由框架更新。
        post.setStatus(parameter.getStatus());
        validatePostDepartment(post);
        if (!this.updateById(post)) {
            throw new ServiceException("修改岗位状态失败");
        }
        invalidatePostSessions(post.getId());
    }

    /**
     * 删除没有成员关联的岗位。
     *
     * @param id 岗位ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:delete", tables =
            @DataPermissionTable(name = "sys_post"))
    public void deletePost(Long id) {
        if (!this.lambdaQuery().eq(SysPost::getId, id).exists()) {
            throw new ServiceException("岗位不存在或无权删除");
        }
        // 完整检查任职关系，包括停用或当前操作者不可见的成员。
        if (userPostService.lambdaQuery().eq(SysUserPost::getPostId, id).exists()) {
            throw new ServiceException("岗位仍有成员，请先解除成员关联");
        }
        if (!this.removeById(id)) {
            throw new ServiceException("删除岗位失败");
        }
    }

    /**
     * 解除明确指定的成员关联，不影响其他成员。
     *
     * @param parameter 岗位ID和本次选择的用户ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:member:assign", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public void deletePostMembers(SysPostMembersDTO parameter) {
        if (!this.lambdaQuery().eq(SysPost::getId, parameter.getPostId()).exists()) {
            throw new ServiceException("岗位不存在或无权配置成员");
        }
        // 停用岗位和停用用户仍可解除关联，但岗位和全部用户必须在配置权限范围内。
        if (userService.lambdaQuery().in(SysUser::getId, parameter.getUserIds()).count()
                != parameter.getUserIds().size()) {
            throw new ServiceException("所选用户不存在或超出成员配置权限");
        }
        List<SysUserPost> relations = userPostService.lambdaQuery()
                .select(SysUserPost::getId, SysUserPost::getUserId)
                .eq(SysUserPost::getPostId, parameter.getPostId())
                .in(SysUserPost::getUserId, parameter.getUserIds()).list();
        if (relations.isEmpty()) {
            return;
        }
        if (!userPostService.removeByIds(relations.stream().map(SysUserPost::getId).toList())) {
            throw new ServiceException("解除岗位成员失败");
        }
        // 仅失效本次实际解除关联的成员，提交失败时不改变登录会话。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                relations.stream().map(SysUserPost::getUserId).toList());
    }

    /**
     * 校验岗位所属部门存在，正常岗位还要求整条祖先链有效。
     *
     * @param post 待保存或分配成员的岗位
     */
    private void validatePostDepartment(SysPost post) {
        if (post.getDeptId() == null) {
            return;
        }
        // 与部门变更共用组织锁，防止校验后部门被删除、移动或停用。
        Integer departmentStatus = deptMapper.getDeptEffectiveStatusById(post.getDeptId());
        if (departmentStatus == null) {
            throw new ServiceException("所属部门不存在");
        }
        if (post.getStatus() == 0 && departmentStatus != 0) {
            throw new ServiceException("正常岗位的所属部门及全部上级部门必须启用");
        }
    }

    /**
     * 在事务提交后使岗位全部成员的会话失效。
     *
     * @param postId 任职信息发生变化的岗位ID
     */
    private void invalidatePostSessions(Long postId) {
        // 直接读取完整任职关系，不能按操作者的用户可见范围裁剪受影响成员。
        List<Long> userIds = userPostService.lambdaQuery().select(SysUserPost::getUserId)
                .eq(SysUserPost::getPostId, postId).list().stream().map(SysUserPost::getUserId).toList();
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
    }
}
