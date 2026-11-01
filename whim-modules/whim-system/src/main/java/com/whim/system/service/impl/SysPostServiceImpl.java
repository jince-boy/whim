package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.whim.system.model.dto.post.SysPostMemberDTO;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostSaveDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位、业务归属及成员维护，成员变更不影响用户角色。
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {
    private final SysDeptMapper deptMapper;
    private final ISysUserService userService;
    private final ISysUserPostService userPostService;
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;

    /**
     * 按名称、编码、部门和状态分页查询授权范围内的岗位。
     *
     * @param query 查询参数
     * @return 岗位分页数据
     */
    @Override
    @DataPermission(permission = "system:post:list", tables = @DataPermissionTable(name = "sys_post"))
    public PageDataVO<SysPostVO> getPostPage(SysPostQueryDTO query) {
        if (Boolean.TRUE.equals(query.getOrganizationWide()) && query.getDeptId() != null) {
            throw new ServiceException("查询全组织岗位时不能同时指定部门");
        }
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<SysPost>()
                .like(StringUtils.isNotBlank(query.getPostName()), SysPost::getPostName, query.getPostName())
                .like(StringUtils.isNotBlank(query.getPostCode()), SysPost::getPostCode, query.getPostCode())
                .eq(query.getDeptId() != null, SysPost::getDeptId, query.getDeptId())
                .isNull(Boolean.TRUE.equals(query.getOrganizationWide()), SysPost::getDeptId)
                .eq(query.getStatus() != null, SysPost::getStatus, query.getStatus())
                .orderByAsc(SysPost::getSort, SysPost::getId);
        return new PageDataVO<>(this.page(query.getPage(), wrapper)
                .convert(post -> BeanConvertUtils.convert(post, SysPostVO.class)));
    }

    /**
     * 分页查询成员或可选用户，同时校验岗位范围和用户范围。
     *
     * @param id 岗位ID
     * @param query 查询参数
     * @return 用户数据权限过滤后的分页数据
     */
    @Override
    @DataPermission(permission = "system:post:member:list", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public PageDataVO<SysPostMemberVO> getPostMemberPage(Long id, SysPostMemberQueryDTO query) {
        getRequiredPost(id);
        return new PageDataVO<>(baseMapper.getPostMemberPage(query.getPage(), id, query));
    }

    /**
     * 查询授权范围内的岗位详情。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    @Override
    @DataPermission(permission = "system:post:detail", tables = @DataPermissionTable(name = "sys_post"))
    public SysPostVO getPostDetail(Long id) {
        return BeanConvertUtils.convert(getRequiredPost(id), SysPostVO.class);
    }

    /**
     * 新增岗位，审计字段由 MyBatis-Plus 自动填充。
     *
     * @param parameter 岗位新增参数
     * @return 新增岗位ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:create", tables = @DataPermissionTable(name = "sys_post"))
    public Long addPost(SysPostSaveDTO parameter) {
        SysPost post = BeanConvertUtils.convert(parameter, SysPost.class);
        post.setPostName(post.getPostName().trim());
        post.setPostCode(post.getPostCode().trim());
        validatePostDepartment(post.getDeptId(), post.getStatus());
        DataPermissionContext.checkOwnership("sys_post", authenticationContext.getUserId(), post.getDeptId());
        if (!this.save(post)) {
            throw new ServiceException("新增岗位失败");
        }
        return post.getId();
    }

    /**
     * 增量添加正常用户，通过数据权限过滤查询校验岗位和全部用户的操作范围。
     *
     * @param id 岗位ID
     * @param parameter 待添加的用户集合
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:member:assign", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public void addPostMembers(Long id, SysPostMemberDTO parameter) {
        SysPost post = getRequiredPost(id);
        if (post.getStatus() != 0) {
            throw new ServiceException("停用岗位不能添加成员");
        }
        validatePostDepartment(post.getDeptId(), 0);
        List<SysUser> users = userService.list(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .in(SysUser::getId, parameter.getUserIds()).eq(SysUser::getStatus, 0));
        if (users.size() != parameter.getUserIds().size()) {
            throw new ServiceException("部分用户不存在、已停用或超出授权范围");
        }
        Set<Long> existingIds = userPostService.list(new LambdaQueryWrapper<SysUserPost>()
                        .select(SysUserPost::getUserId).eq(SysUserPost::getPostId, id)
                        .in(SysUserPost::getUserId, parameter.getUserIds()))
                .stream().map(SysUserPost::getUserId).collect(Collectors.toSet());
        List<SysUserPost> relations = new ArrayList<>();
        for (SysUser user : users) {
            if (!existingIds.contains(user.getId())) {
                SysUserPost relation = new SysUserPost();
                relation.setUserId(user.getId());
                relation.setPostId(id);
                relations.add(relation);
            }
        }
        if (relations.isEmpty()) {
            return;
        }
        if (!userPostService.saveBatch(relations)) {
            throw new ServiceException("添加岗位成员失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                relations.stream().map(SysUserPost::getUserId).toList());
    }

    /**
     * 按ID修改岗位名称、编码、部门、排序、状态和备注。
     *
     * @param parameter 岗位修改参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:update", tables = @DataPermissionTable(name = "sys_post"))
    public void updatePost(SysPostUpdateDTO parameter) {
        Long id = parameter.getId();
        SysPost previous = getRequiredPost(id);
        SysPost post = BeanConvertUtils.convert(parameter, SysPost.class);
        post.setPostName(post.getPostName().trim());
        post.setPostCode(post.getPostCode().trim());
        validatePostDepartment(post.getDeptId(), post.getStatus());
        if (!Objects.equals(previous.getDeptId(), post.getDeptId())) {
            DataPermissionContext.checkOwnership("sys_post", previous.getCreateBy(), post.getDeptId());
        }
        if (!this.updateById(post)) {
            throw new ServiceException("修改岗位失败");
        }
        invalidatePostSessions(id);
    }

    /**
     * 修改岗位状态，启用前校验部门及祖先均有效。
     *
     * @param parameter 包含岗位ID和状态的修改参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:status", tables = @DataPermissionTable(name = "sys_post"))
    public void updatePostStatus(SysOrganizationStatusDTO parameter) {
        Long id = parameter.getId();
        Integer status = parameter.getStatus();
        SysPost post = getRequiredPost(id);
        if (post.getStatus().equals(status)) {
            return;
        }
        if (status == 0) {
            validatePostDepartment(post.getDeptId(), 0);
        }
        post.setStatus(status);
        if (!this.updateById(post)) {
            throw new ServiceException("修改岗位状态失败");
        }
        invalidatePostSessions(id);
    }

    /**
     * 删除没有任何成员关联的岗位，编码在逻辑删除后可复用。
     *
     * @param id 岗位ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:delete", tables = @DataPermissionTable(name = "sys_post"))
    public void deletePost(Long id) {
        getRequiredPost(id);
        if (userPostService.exists(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getPostId, id))) {
            throw new ServiceException("岗位仍有成员，请先解除成员关联");
        }
        if (!this.removeById(id)) {
            throw new ServiceException("删除岗位失败");
        }
    }

    /**
     * 解除指定用户的岗位关联，允许移除停用用户，不覆盖其他成员。
     *
     * @param id 岗位ID
     * @param parameter 待解除关联的用户集合
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:post:member:assign", tables = {
            @DataPermissionTable(name = "sys_post"),
            @DataPermissionTable(name = "sys_user", userColumn = "id")})
    public void deletePostMembers(Long id, SysPostMemberDTO parameter) {
        getRequiredPost(id);
        long visibleCount = userService.count(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, parameter.getUserIds()));
        if (visibleCount != parameter.getUserIds().size()) {
            throw new ServiceException("部分用户不存在或超出授权范围");
        }
        LambdaQueryWrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
                .eq(SysUserPost::getPostId, id).in(SysUserPost::getUserId, parameter.getUserIds());
        List<SysUserPost> relations = userPostService.list(wrapper);
        if (relations.isEmpty()) {
            return;
        }
        if (!userPostService.remove(wrapper)) {
            throw new ServiceException("解除岗位成员关联失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                relations.stream().map(SysUserPost::getUserId).toList());
    }

    /**
     * 获取本次操作有权访问的岗位。
     *
     * @param id 岗位ID
     * @return 岗位实体
     */
    private SysPost getRequiredPost(Long id) {
        SysPost post = this.getById(id);
        if (post == null) {
            throw new ServiceException("岗位不存在或无权访问");
        }
        return post;
    }

    /**
     * 校验所属部门存在，正常岗位还要求部门及祖先均启用。
     *
     * @param deptId 所属部门ID，null表示全组织岗位
     * @param status 岗位状态
     */
    private void validatePostDepartment(Long deptId, Integer status) {
        if (deptId == null) {
            return;
        }
        if (status == 0) {
            if (deptMapper.getValidDepartmentsByIds(Set.of(deptId)).isEmpty()) {
                throw new ServiceException("所属部门不存在、已停用或祖先部门已停用");
            }
        } else if (deptMapper.getDepartmentReferenceById(deptId) == null) {
            throw new ServiceException("所属部门不存在");
        }
    }

    /**
     * 在事务提交后失效所有岗位成员的登录快照。
     *
     * @param id 岗位ID
     */
    private void invalidatePostSessions(Long id) {
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                userPostService.list(new LambdaQueryWrapper<SysUserPost>()
                                .select(SysUserPost::getUserId).eq(SysUserPost::getPostId, id))
                        .stream().map(SysUserPost::getUserId).toList());
    }
}
