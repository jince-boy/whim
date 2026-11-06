package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.user.SysUserAddDTO;
import com.whim.system.model.dto.user.SysUserPasswordDTO;
import com.whim.system.model.dto.user.SysUserQueryDTO;
import com.whim.system.model.dto.user.SysUserUpdateDTO;
import com.whim.system.model.vo.user.SysUserDetailVO;
import com.whim.system.model.vo.user.SysUserVO;
import com.whim.system.service.ISysUserService;
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
 * @date 2026/11/06
 * @description 系统用户管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/user")
public class SysUserController {
    private final ISysUserService userService;

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
     * 新增用户并保存岗位任职关系。
     *
     * @param parameter 用户新增参数
     * @return 新增用户成功响应
     */
    @PostMapping("/add")
    @SystemCheckPermission("system:user:create")
    public Result<Void> addUser(@Valid @RequestBody SysUserAddDTO parameter) {
        userService.addUser(parameter);
        return Result.success("新增用户成功");
    }

    /**
     * 修改用户及岗位，提交后失效登录会话。
     *
     * @param parameter 用户ID及完整修改字段
     * @return 修改用户成功响应
     */
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
    @PutMapping("/resetPassword")
    @SystemCheckPermission("system:user:resetPassword")
    public Result<Void> resetUserPassword(@Valid @RequestBody SysUserPasswordDTO parameter) {
        userService.resetUserPassword(parameter);
        return Result.success("修改密码成功");
    }

    /**
     * 删除用户及关联关系，禁止删除当前账号或仍担任部门负责人的用户。
     *
     * @param id 用户ID
     * @return 删除用户成功响应
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:user:delete")
    public Result<Void> deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return Result.success("删除用户成功");
    }
}
