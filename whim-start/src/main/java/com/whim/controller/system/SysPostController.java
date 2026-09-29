package com.whim.controller.system;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.post.PostSaveDTO;
import com.whim.system.model.dto.post.PostStatusDTO;
import com.whim.system.model.dto.post.PostMemberAssignDTO;
import com.whim.system.model.vo.post.PostVO;
import com.whim.system.service.ISysPostService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统岗位表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/post")
public class SysPostController {

    /**
     * 系统岗位表服务对象
     */
    private final ISysPostService sysPostService;

    /** 分页查询本次列表权限可见的岗位。 */
    @GetMapping
    @SystemCheckPermission("system:post:list")
    public Result<PageDataVO<PostVO>> pagePosts(@Valid @ModelAttribute PageQueryDTO query) {
        return Result.success("岗位查询成功", sysPostService.pageCurrentTenantPosts(query));
    }

    /** 查询本次详情权限可见的岗位。 */
    @GetMapping("/{postId}")
    @SystemCheckPermission("system:post:detail")
    public Result<PostVO> getPost(@PathVariable Long postId) {
        return Result.success("岗位详情查询成功", sysPostService.getCurrentTenantPost(postId));
    }

    /** 查询本次成员查看权限可见岗位的成员。 */
    @GetMapping("/{postId}/members")
    @SystemCheckPermission("system:post:member:list")
    public Result<Set<Long>> getPostMemberIds(@PathVariable Long postId) {
        return Result.success("岗位成员查询成功", sysPostService.getCurrentTenantPostMemberIds(postId));
    }

    /** 创建当前租户岗位。 */
    @PostMapping
    @SystemCheckPermission("system:post:create")
    public Result<Long> createPost(@RequestBody @Valid PostSaveDTO request) {
        return Result.success("岗位创建成功", sysPostService.createCurrentTenantPost(request));
    }

    /** 修改本次更新权限可操作的岗位。 */
    @PutMapping("/{postId}")
    @SystemCheckPermission("system:post:update")
    public Result<Void> updatePost(@PathVariable Long postId, @RequestBody @Valid PostSaveDTO request) {
        sysPostService.updateCurrentTenantPost(postId, request);
        return Result.success("岗位修改成功");
    }

    /** 启用或停用本次状态权限可操作的岗位。 */
    @PutMapping("/{postId}/status")
    @SystemCheckPermission("system:post:status")
    public Result<Void> setPostStatus(@PathVariable Long postId, @RequestBody @Valid PostStatusDTO request) {
        sysPostService.setCurrentTenantPostStatus(postId, request.getStatus());
        return Result.success("岗位状态修改成功");
    }

    /** 覆盖本次成员分配权限可操作岗位的成员。 */
    @PutMapping("/{postId}/members")
    @SystemCheckPermission("system:post:member:assign")
    public Result<Void> replacePostMembers(@PathVariable Long postId,
                                           @RequestBody @Valid PostMemberAssignDTO request) {
        sysPostService.replaceCurrentTenantPostMembers(postId, request.getUserIds());
        return Result.success("岗位成员分配成功");
    }

    /** 软删本次删除权限可操作的岗位。 */
    @DeleteMapping("/{postId}")
    @SystemCheckPermission("system:post:delete")
    public Result<Void> deletePost(@PathVariable Long postId) {
        sysPostService.deleteCurrentTenantPost(postId);
        return Result.success("岗位删除成功");
    }
}

