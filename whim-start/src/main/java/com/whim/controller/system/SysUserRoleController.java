package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.vo.user.SysUserRolesVO;
import com.whim.system.model.dto.user.SysUserRoleDTO;
import com.whim.system.service.ISysUserService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户角色查询与分配接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/userRole")
public class SysUserRoleController {
    private final ISysUserService userService;

    /**
     * 查询用户当前角色和可分配的角色。
     *
     * @param id 用户ID
     * @return 当前角色及候选项
     */
    @GetMapping("/list")
    @SystemCheckPermission("system:userRole:list")
    public Result<SysUserRolesVO> getUserRoles(@RequestParam Long id) {
        return Result.success("用户角色查询成功", userService.getUserRoles(id));
    }

    /**
     * 校验角色可授予范围后更新用户角色。
     *
     * @param parameter 用户ID及完整角色集合
     * @return 分配角色成功响应
     */
    @PutMapping("/assign")
    @SystemCheckPermission("system:userRole:assign")
    public Result<Void> assignUserRoles(@Valid @RequestBody SysUserRoleDTO parameter) {
        userService.assignUserRoles(parameter);
        return Result.success("分配角色成功");
    }
}
