package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.post.SysPostAddDTO;
import com.whim.system.model.dto.post.SysPostQueryDTO;
import com.whim.system.model.dto.post.SysPostUpdateDTO;
import com.whim.system.model.vo.post.SysPostVO;
import com.whim.system.service.ISysPostService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 系统岗位基础信息管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/post")
public class SysPostController {
    private final ISysPostService postService;

    /**
     * 分页查询岗位，选择部门时包含该部门及全部下级部门。
     *
     * @param query 岗位筛选及分页条件
     * @return 岗位分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:post:list")
    public Result<PageDataVO<SysPostVO>> getPostPage(@Valid SysPostQueryDTO query) {
        return Result.success("岗位查询成功", postService.getPostPage(query));
    }

    /**
     * 查询岗位详情。
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:post:detail")
    public Result<SysPostVO> getPostDetail(@RequestParam Long id) {
        return Result.success("岗位详情查询成功", postService.getPostDetail(id));
    }

    /**
     * 新增岗位。
     *
     * @param parameter 岗位新增参数
     * @return 新增结果
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:post:create")
    public Result<Void> addPost(@Valid @RequestBody SysPostAddDTO parameter) {
        postService.addPost(parameter);
        return Result.success("新增岗位成功");
    }

    /**
     * 完整修改岗位。
     *
     * @param parameter 岗位ID及修改字段
     * @return 修改结果
     */
    @PutMapping("/update")
    @SystemCheckPermission("system:post:update")
    public Result<Void> updatePost(@Valid @RequestBody SysPostUpdateDTO parameter) {
        postService.updatePost(parameter);
        return Result.success("修改岗位成功");
    }

    /**
     * 删除没有成员的岗位。
     *
     * @param id 岗位ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:post:delete")
    public Result<Void> deletePost(@RequestParam Long id) {
        postService.deletePost(id);
        return Result.success("删除岗位成功");
    }
}
