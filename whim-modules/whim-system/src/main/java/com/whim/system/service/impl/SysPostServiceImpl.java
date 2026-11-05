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
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUserPost;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysUserPostService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位管理与成员查询，岗位只表达任职关系，不授予角色。
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
     * 分页查询岗位已分配成员。
     *
     * @param query 岗位ID及用户筛选条件
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
        // 编码、名称、部门或状态变化会影响登录中的任职信息；排序和备注不影响。
        if (!originalPost.getPostCode().equals(post.getPostCode())
                || !originalPost.getPostName().equals(post.getPostName())
                || !Objects.equals(originalPost.getDeptId(), post.getDeptId())
                || !originalPost.getStatus().equals(post.getStatus())) {
            // 完整读取受影响成员，不能按用户可见范围裁剪；仅在事务提交后失效会话。
            List<Long> userIds = userPostService.lambdaQuery().select(SysUserPost::getUserId)
                    .eq(SysUserPost::getPostId, post.getId()).list().stream().map(SysUserPost::getUserId).toList();
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
        }
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
     * 校验岗位所属部门存在，正常岗位还要求整条祖先链有效。
     *
     * @param post 待保存的岗位
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

}
