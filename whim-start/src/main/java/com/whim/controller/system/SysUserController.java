package com.whim.controller.system;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.user.UserCreateDTO;
import com.whim.system.model.dto.user.UserDepartmentDTO;
import com.whim.system.model.dto.user.UserPasswordDTO;
import com.whim.system.model.dto.user.UserRoleAssignDTO;
import com.whim.system.model.dto.user.UserStatusDTO;
import com.whim.system.model.dto.user.UserUpdateDTO;
import com.whim.system.model.vo.user.UserVO;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
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
 * @date 2026/09/30
 * @description 系统用户、主部门和角色授权接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {
    private final ISysUserService sysUserService;
    private final ISysUserRoleService sysUserRoleService;

    /** 分页查询本次操作可见用户。 */
    @GetMapping
    @SystemCheckPermission("system:user:list")
    public Result<PageDataVO<UserVO>> pageUsers(@Valid @ModelAttribute PageQueryDTO query) {
        return Result.success("用户查询成功", sysUserService.pageUsers(query));
    }

    /** 查询本次操作可见用户详情。 */
    @GetMapping("/{userId}")
    @SystemCheckPermission("system:user:detail")
    public Result<UserVO> getUser(@PathVariable Long userId) {
        return Result.success("用户详情查询成功", sysUserService.getUser(userId));
    }

    /** 创建账号，角色和岗位通过独立操作分配。 */
    @PostMapping
    @SystemCheckPermission("system:user:create")
    public Result<Long> createUser(@RequestBody @Valid UserCreateDTO request) {
        return Result.success("用户创建成功", sysUserService.createUser(request));
    }

    /** 修改用户基础信息。 */
    @PutMapping("/{userId}")
    @SystemCheckPermission("system:user:update")
    public Result<Void> updateUser(@PathVariable Long userId, @RequestBody @Valid UserUpdateDTO request) {
        sysUserService.updateUser(userId, request);
        return Result.success("用户修改成功");
    }

    /** 分配用户主部门，重新核验迁移后的角色数据范围。 */
    @PutMapping("/{userId}/department")
    @SystemCheckPermission("system:user:department")
    public Result<Void> setDepartment(@PathVariable Long userId, @RequestBody @Valid UserDepartmentDTO request) {
        sysUserService.setUserDepartment(userId, request.getDeptId());
        return Result.success("用户主部门修改成功");
    }

    /** 修改用户状态并撤销旧会话。 */
    @PutMapping("/{userId}/status")
    @SystemCheckPermission("system:user:status")
    public Result<Void> setUserStatus(@PathVariable Long userId, @RequestBody @Valid UserStatusDTO request) {
        sysUserService.setUserStatus(userId, request.getStatus());
        return Result.success("用户状态修改成功");
    }

    /** 独立授权重置密码，成功后撤销全部旧会话。 */
    @PutMapping("/{userId}/password")
    @SystemCheckPermission("system:user:resetPassword")
    public Result<Void> resetPassword(@PathVariable Long userId, @RequestBody @Valid UserPasswordDTO request) {
        sysUserService.resetPassword(userId, request.getPassword());
        return Result.success("用户密码重置成功");
    }

    /** 查询本次操作可见用户的角色ID。 */
    @GetMapping("/{userId}/roles")
    @SystemCheckPermission("system:userRole:list")
    public Result<Set<Long>> getUserRoleIds(@PathVariable Long userId) {
        return Result.success("用户角色查询成功", sysUserRoleService.getRoleIds(userId));
    }

    /** 覆盖用户角色并核验每个候选角色的委派范围。 */
    @PutMapping("/{userId}/roles")
    @SystemCheckPermission("system:userRole:assign")
    public Result<Void> replaceUserRoles(@PathVariable Long userId, @RequestBody @Valid UserRoleAssignDTO request) {
        sysUserRoleService.replaceRoles(userId, request.getRoleIds());
        return Result.success("用户角色分配成功");
    }

    /** 删除用户并清理角色和岗位关联。 */
    @DeleteMapping("/{userId}")
    @SystemCheckPermission("system:user:delete")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        sysUserService.deleteUser(userId);
        return Result.success("用户删除成功");
    }
}

