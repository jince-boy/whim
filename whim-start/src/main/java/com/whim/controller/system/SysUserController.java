package com.whim.controller.system;

import com.whim.core.annotation.RequestDecrypt;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import cn.dev33.satoken.annotation.SaMode;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.user.SysUserAddDTO;
import com.whim.system.model.dto.user.SysUserAvatarQueryDTO;
import com.whim.system.model.dto.user.SysUserAvatarUploadDTO;
import com.whim.system.model.dto.user.SysUserPasswordDTO;
import com.whim.system.model.dto.user.SysUserQueryDTO;
import com.whim.system.model.dto.user.SysUserRoleDTO;
import com.whim.system.model.dto.user.SysUserTwoFactorResetDTO;
import com.whim.system.model.dto.user.SysUserUpdateDTO;
import com.whim.system.model.vo.user.SysUserDetailVO;
import com.whim.system.model.vo.user.SysUserRolesVO;
import com.whim.system.model.vo.user.SysUserVO;
import com.whim.system.service.IAuthService;
import com.whim.system.service.ISysUserService;
import com.whim.web.annotation.XssIgnore;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户资料、角色分配及头像接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/user")
public class SysUserController {
    private final ISysUserService userService;
    private final IAuthService authService;

    /**
     * 按部门分支和用户信息分页查询可见用户。
     *
     * @param query 用户筛选及分页条件
     * @return 用户分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:user:list")
    public Result<PageDataVO<SysUserVO>> getUserPage(@Valid SysUserQueryDTO query) {
        return Result.success("用户查询成功", userService.getUserPage(query));
    }

    /**
     * 查询用户编辑详情和当前岗位。
     *
     * @param id 用户ID
     * @return 用户编辑详情
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:user:detail")
    public Result<SysUserDetailVO> getUserDetail(@RequestParam Long id) {
        return Result.success("用户详情查询成功", userService.getUserDetail(id));
    }

    /**
     * 查询用户当前角色和可分配的角色。
     *
     * @param id 用户ID
     * @return 当前角色及候选项
     */
    @GetMapping("/roles")
    @SystemCheckPermission("system:userRole:list")
    public Result<SysUserRolesVO> getUserRoles(@RequestParam Long id) {
        return Result.success("用户角色查询成功", userService.getUserRoles(id));
    }

    /**
     * 为已登录用户返回头像缩略图或未经缩放的头像原文件。
     *
     * @param query 头像文件及预览方式
     * @param response 图片响应，原文件以附件返回
     * @throws IOException 写入缩略图响应失败
     */
    @GetMapping("/avatar")
    public void getUserAvatar(@Valid @ModelAttribute SysUserAvatarQueryDTO query,
                              HttpServletResponse response) throws IOException {
        userService.getUserAvatar(query, response);
    }

    /**
     * 新增用户并保存岗位任职关系。
     *
     * @param parameter 用户新增参数
     * @return 新增用户成功响应
     */
    @Log(title = "用户管理", type = LogType.INSERT, excludeFields = "password")
    @PostMapping("/add")
    @SystemCheckPermission("system:user:create")
    public Result<Void> addUser(@Valid @RequestBody SysUserAddDTO parameter) {
        userService.addUser(parameter);
        return Result.success("新增用户成功");
    }

    /**
     * 上传头像并返回地址，由新增或修改用户请求保存关联。
     *
     * @param parameter 本地头像图片
     * @return 头像地址
     */
    @Log(title = "上传头像", type = LogType.UPDATE, excludeFields = "file")
    @PostMapping(value = "/uploadAvatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SystemCheckPermission(value = {"system:user:create", "system:user:update"}, mode = SaMode.OR)
    public Result<String> addUserAvatar(@Valid @ModelAttribute SysUserAvatarUploadDTO parameter) {
        return Result.success("头像上传成功", userService.addUserAvatar(parameter));
    }

    /**
     * 修改用户及岗位，提交后失效登录会话。
     *
     * @param parameter 用户ID及完整修改字段
     * @return 修改用户成功响应
     */
    @Log(title = "用户管理", type = LogType.UPDATE)
    @PutMapping("/update")
    @SystemCheckPermission("system:user:update")
    public Result<Void> updateUser(@Valid @RequestBody SysUserUpdateDTO parameter) {
        userService.updateUser(parameter);
        return Result.success("修改用户成功");
    }

    /**
     * 修改用户密码并在事务提交后使其全部会话失效。
     *
     * @param parameter 用户ID及新密码
     * @return 修改密码成功响应
     */
    @Log(title = "重置用户密码", type = LogType.UPDATE, excludeFields = "password")
    @PutMapping("/resetPassword")
    @SystemCheckPermission("system:user:resetPassword")
    public Result<Void> resetUserPassword(@Valid @RequestBody SysUserPasswordDTO parameter) {
        userService.resetUserPassword(parameter);
        return Result.success("修改密码成功");
    }

    /**
     * 由超级管理员解除目标用户丢失的两步验证绑定。
     *
     * @param parameter 目标用户、预期绑定及操作者当前密码
     * @return 重置结果
     */
    @Log(title = "重置用户两步验证", type = LogType.UPDATE, excludeFields = "currentPassword")
    @RequestDecrypt
    @XssIgnore
    @PutMapping("/resetTwoFactor")
    public Result<Void> resetUserTwoFactor(@Valid @RequestBody SysUserTwoFactorResetDTO parameter) {
        authService.resetUserTwoFactor(parameter);
        return Result.success("两步验证已重置，该用户需重新登录");
    }

    /**
     * 校验角色可授予范围后更新用户角色。
     *
     * @param parameter 用户ID及完整角色集合
     * @return 分配角色成功响应
     */
    @Log(title = "分配角色", type = LogType.UPDATE)
    @PutMapping("/assignRoles")
    @SystemCheckPermission("system:userRole:assign")
    public Result<Void> assignUserRoles(@Valid @RequestBody SysUserRoleDTO parameter) {
        userService.assignUserRoles(parameter);
        return Result.success("分配角色成功");
    }

    /**
     * 删除用户及关联关系，禁止删除当前账号或仍担任部门负责人的用户。
     *
     * @param id 用户ID
     * @return 删除用户成功响应
     */
    @Log(title = "用户管理", type = LogType.DELETE)
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:user:delete")
    public Result<Void> deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功");
    }
}
