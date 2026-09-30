package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.role.RolePermissionAssignDTO;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.dto.role.RoleStatusDTO;
import com.whim.system.model.dto.role.RoleDataScopeDTO;
import com.whim.system.model.dto.role.RolePermissionDataScopeDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    /** 查询系统角色。 */
    @GetMapping
    @SystemCheckPermission("system:role:list")
    public Result<List<RoleVO>> listRoles() {
        return Result.success("角色查询成功", sysRoleService.listRoles());
    }

    /** 创建系统角色。 */
    @PostMapping
    @SystemCheckPermission("system:role:create")
    public Result<Long> createRole(@RequestBody @Valid RoleSaveDTO request) {
        return Result.success("角色创建成功", sysRoleService.createRole(request));
    }

    /** 修改系统角色。 */
    @PutMapping("/{roleId}")
    @SystemCheckPermission("system:role:update")
    public Result<Void> updateRole(@PathVariable Long roleId, @RequestBody @Valid RoleSaveDTO request) {
        sysRoleService.updateRole(roleId, request);
        return Result.success("角色修改成功");
    }

    /** 修改系统角色状态。 */
    @PutMapping("/{roleId}/status")
    @SystemCheckPermission("system:role:status")
    public Result<Void> setRoleStatus(@PathVariable Long roleId, @RequestBody @Valid RoleStatusDTO request) {
        sysRoleService.setRoleStatus(roleId, request.getStatus());
        return Result.success("角色状态修改成功");
    }

    /** 查询系统角色的数据范围。 */
    @GetMapping("/{roleId}/dataScope")
    @SystemCheckPermission("system:role:dataScope:list")
    public Result<RoleDataScopeVO> getRoleDataScope(@PathVariable Long roleId) {
        return Result.success("角色数据范围查询成功", sysRoleService.getDataScope(roleId));
    }

    /** 覆盖系统角色的数据范围。 */
    @PutMapping("/{roleId}/dataScope")
    @SystemCheckPermission("system:role:dataScope:assign")
    public Result<Void> replaceRoleDataScope(@PathVariable Long roleId,
                                              @RequestBody @Valid RoleDataScopeDTO request) {
        sysRoleService.replaceDataScope(roleId, request.getDataScope(), request.getDeptIds());
        return Result.success("角色数据范围修改成功");
    }

    /** 查询系统角色已分配的权限ID。 */
    @GetMapping("/{roleId}/permissions")
    @SystemCheckPermission("system:rolePermission:list")
    public Result<Set<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        return Result.success("角色权限查询成功", sysRolePermissionService.getPermissionIds(roleId));
    }

    /** 覆盖系统角色的权限。 */
    @PutMapping("/{roleId}/permissions")
    @SystemCheckPermission("system:rolePermission:assign")
    public Result<Void> replaceRolePermissions(@PathVariable Long roleId,
                                               @RequestBody @Valid RolePermissionAssignDTO request) {
        sysRolePermissionService.replacePermissions(roleId, request.getPermissionIds());
        return Result.success("角色权限分配成功");
    }
    /** 查询某个操作的覆盖数据范围，空范围编码表示继承角色默认值。 */
    @GetMapping("/{roleId}/permissions/{permissionId}/dataScope")
    @SystemCheckPermission("system:role:dataScope:list")
    public Result<RoleDataScopeVO> getPermissionDataScope(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return Result.success("操作数据范围查询成功", sysRolePermissionService.getPermissionDataScope(roleId, permissionId));
    }

    /** 设置某个操作的数据范围覆盖，或恢复继承角色默认值。 */
    @PutMapping("/{roleId}/permissions/{permissionId}/dataScope")
    @SystemCheckPermission("system:role:dataScope:assign")
    public Result<Void> replacePermissionDataScope(@PathVariable Long roleId, @PathVariable Long permissionId,
                                                  @RequestBody @Valid RolePermissionDataScopeDTO request) {
        sysRolePermissionService.replacePermissionDataScope(roleId, permissionId, request.getDataScope(),
                request.getDeptIds());
        return Result.success("操作数据范围修改成功");
    }

    /** 删除未被用户持有的普通角色。 */
    @DeleteMapping("/{roleId}")
    @SystemCheckPermission("system:role:delete")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        sysRoleService.deleteRole(roleId);
        return Result.success("角色删除成功");
    }
}

