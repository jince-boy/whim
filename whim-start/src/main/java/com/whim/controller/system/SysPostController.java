package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.dto.post.SysPostMemberDTO;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostSaveDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.service.ISysPostService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 系统岗位及岗位成员管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/post")
public class SysPostController {
    private final ISysPostService postService;

    /**
     * 分页查询岗位，复用内置岗位列表权限。
     *
     * @param query 查询参数
     * @return 岗位分页数据
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:post:list")
    public Result<PageDataVO<SysPostVO>> getPostPage(@Valid SysPostQueryDTO query) {
        return Result.success("岗位分页查询成功", postService.getPostPage(query));
    }

    /**
     * 查询岗位详情。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    @GetMapping("/{id}")
    @SystemCheckPermission("system:post:detail")
    public Result<SysPostVO> getPostDetail(@PathVariable Long id) {
        return Result.success("岗位详情查询成功", postService.getPostDetail(id));
    }

    /**
     * 分页查询已分配成员或尚未分配的正常用户。
     *
     * @param id 岗位ID
     * @param query 用户筛选参数
     * @return 用户分页数据
     */
    @GetMapping("/{id}/members")
    @SystemCheckPermission("system:post:member:list")
    public Result<PageDataVO<SysPostMemberVO>> getPostMemberPage(
            @PathVariable Long id,
            @Valid SysPostMemberQueryDTO query) {
        return Result.success("岗位成员查询成功", postService.getPostMemberPage(id, query));
    }

    /**
     * 新增岗位。
     *
     * @param parameter 岗位新增参数
     * @return 新增岗位ID
     */
    @PostMapping
    @SystemCheckPermission("system:post:create")
    public Result<Long> addPost(@Valid @RequestBody SysPostSaveDTO parameter) {
        return Result.success("岗位新增成功", postService.addPost(parameter));
    }

    /**
     * 按ID修改岗位，包括表单提交的状态。
     *
     * @param parameter 岗位修改参数
     * @return 修改结果
     */
    @PutMapping
    @SystemCheckPermission("system:post:update")
    public Result<Void> updatePost(@Valid @RequestBody SysPostUpdateDTO parameter) {
        postService.updatePost(parameter);
        return Result.success("岗位修改成功");
    }

    /**
     * 使用独立权限修改岗位状态。
     *
     * @param parameter 包含岗位ID和状态的修改参数
     * @return 状态修改结果
     */
    @PutMapping("/status")
    @SystemCheckPermission("system:post:status")
    public Result<Void> updatePostStatus(@Valid @RequestBody SysOrganizationStatusDTO parameter) {
        postService.updatePostStatus(parameter);
        return Result.success("岗位状态修改成功");
    }

    /**
     * 删除没有成员关联的岗位。
     *
     * @param id 岗位ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SystemCheckPermission("system:post:delete")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("岗位删除成功");
    }

    /**
     * 增量添加岗位成员，不改变用户角色。
     *
     * @param id 岗位ID
     * @param parameter 待添加的用户集合
     * @return 添加结果
     */
    @PostMapping("/{id}/members")
    @SystemCheckPermission("system:post:member:assign")
    public Result<Void> addPostMembers(@PathVariable Long id,
                                      @Valid @RequestBody SysPostMemberDTO parameter) {
        postService.addPostMembers(id, parameter);
        return Result.success("岗位成员添加成功");
    }

    /**
     * 解除指定岗位成员关联，保留其他成员。
     *
     * @param id 岗位ID
     * @param parameter 待解除关联的用户集合
     * @return 解除关联结果
     */
    @DeleteMapping("/{id}/members")
    @SystemCheckPermission("system:post:member:assign")
    public Result<Void> deletePostMembers(@PathVariable Long id,
                                         @Valid @RequestBody SysPostMemberDTO parameter) {
        postService.deletePostMembers(id, parameter);
        return Result.success("岗位成员关联解除成功");
    }
}
