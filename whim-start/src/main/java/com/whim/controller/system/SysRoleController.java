package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.role.RolePermissionAssignDTO;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.dto.role.RoleStatusDTO;
import com.whim.system.model.dto.role.RoleDataScopeDTO;
import com.whim.system.model.vo.role.RoleDataScopeVO;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统角色表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController {

    /**
     * 系统角色表服务对象
     */
    private final ISysRoleService sysRoleService;
    private final ISysRolePermissionService sysRolePermissionService;

    /** 查询当前租户角色。 */
    @GetMapping
    @SystemCheckPermission("system:role:list")
    public Result<List<RoleVO>> listRoles() {
        return Result.success("角色查询成功", sysRoleService.listCurrentTenantRoles());
    }

    /** 创建当前租户角色。 */
    @PostMapping
    @SystemCheckPermission("system:role:create")
    public Result<Long> createRole(@RequestBody @Valid RoleSaveDTO request) {
        return Result.success("角色创建成功", sysRoleService.createCurrentTenantRole(request));
    }

    /** 修改当前租户角色。 */
    @PutMapping("/{roleId}")
    @SystemCheckPermission("system:role:update")
    public Result<Void> updateRole(@PathVariable Long roleId, @RequestBody @Valid RoleSaveDTO request) {
        sysRoleService.updateCurrentTenantRole(roleId, request);
        return Result.success("角色修改成功");
    }

    /** 修改当前租户角色状态。 */
    @PutMapping("/{roleId}/status")
    @SystemCheckPermission("system:role:status")
    public Result<Void> setRoleStatus(@PathVariable Long roleId, @RequestBody @Valid RoleStatusDTO request) {
        sysRoleService.setCurrentTenantRoleStatus(roleId, request.getStatus());
        return Result.success("角色状态修改成功");
    }

    /** 查询当前租户角色的数据范围。 */
    @GetMapping("/{roleId}/dataScope")
    @SystemCheckPermission("system:role:dataScope")
    public Result<RoleDataScopeVO> getRoleDataScope(@PathVariable Long roleId) {
        return Result.success("角色数据范围查询成功", sysRoleService.getCurrentTenantDataScope(roleId));
    }

    /** 覆盖当前租户角色的数据范围。 */
    @PutMapping("/{roleId}/dataScope")
    @SystemCheckPermission("system:role:dataScope")
    public Result<Void> replaceRoleDataScope(@PathVariable Long roleId,
                                              @RequestBody @Valid RoleDataScopeDTO request) {
        sysRoleService.replaceCurrentTenantDataScope(roleId, request.getDataScope(), request.getDeptIds());
        return Result.success("角色数据范围修改成功");
    }

    /** 查询当前租户角色已分配的权限ID。 */
    @GetMapping("/{roleId}/permissions")
    @SystemCheckPermission("system:rolePermission:list")
    public Result<Set<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        return Result.success("角色权限查询成功", sysRolePermissionService.getCurrentTenantPermissionIds(roleId));
    }

    /** 覆盖当前租户角色的权限。 */
    @PutMapping("/{roleId}/permissions")
    @SystemCheckPermission("system:rolePermission:assign")
    public Result<Void> replaceRolePermissions(@PathVariable Long roleId,
                                               @RequestBody @Valid RolePermissionAssignDTO request) {
        sysRolePermissionService.replaceCurrentTenantPermissions(roleId, request.getPermissionIds());
        return Result.success("角色权限分配成功");
    }
}

