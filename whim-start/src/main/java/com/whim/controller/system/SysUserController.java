package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.user.UserRoleAssignDTO;
import com.whim.system.model.dto.user.UserStatusDTO;
import com.whim.system.service.ISysUserRoleService;
import com.whim.system.service.ISysUserService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController {

    /**
     * 系统用户表服务对象
     */
    private final ISysUserService sysUserService;
    private final ISysUserRoleService sysUserRoleService;

    /** 修改全局用户状态。 */
    @PutMapping("/{userId}/status")
    @SystemCheckPermission("system:platform:user:status")
    public Result<Void> setUserStatus(@PathVariable Long userId, @RequestBody @Valid UserStatusDTO request) {
        sysUserService.setUserStatus(userId, request.getStatus());
        return Result.success("用户状态修改成功");
    }

    /** 查询成员在当前租户的角色ID。 */
    @GetMapping("/{userId}/roles")
    @SystemCheckPermission("system:userRole:list")
    public Result<Set<Long>> getUserRoleIds(@PathVariable Long userId) {
        return Result.success("成员角色查询成功", sysUserRoleService.getCurrentTenantRoleIds(userId));
    }

    /** 覆盖成员在当前租户的角色。 */
    @PutMapping("/{userId}/roles")
    @SystemCheckPermission("system:userRole:assign")
    public Result<Void> replaceUserRoles(@PathVariable Long userId, @RequestBody @Valid UserRoleAssignDTO request) {
        sysUserRoleService.replaceCurrentTenantRoles(userId, request.getRoleIds());
        return Result.success("成员角色分配成功");
    }
}

