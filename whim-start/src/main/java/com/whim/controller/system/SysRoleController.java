package com.whim.controller.system;

import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.role.SysRoleAddDTO;
import com.whim.system.model.dto.role.SysRoleDataScopeDTO;
import com.whim.system.model.dto.role.SysRolePermissionDTO;
import com.whim.system.model.dto.role.SysRoleQueryDTO;
import com.whim.system.model.dto.role.SysRoleStatusDTO;
import com.whim.system.model.dto.role.SysRoleUpdateDTO;
import com.whim.system.model.dto.role.SysRoleUserDTO;
import com.whim.system.model.dto.role.SysRoleUserQueryDTO;
import com.whim.system.model.vo.role.SysRoleDataScopeVO;
import com.whim.system.model.vo.role.SysRoleUserVO;
import com.whim.system.model.vo.role.SysRoleVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
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
 * @date 2026/11/14
 * @description 角色资料、授权配置及成员管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/role")
public class SysRoleController {
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;

    /**
     * 分页查询角色，所有角色均按实际授权配置维护。
     *
     * @param query 角色筛选及分页条件
     * @return 角色分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:role:list")
    public Result<PageDataVO<SysRoleVO>> getRolePage(@Valid SysRoleQueryDTO query) {
        return Result.success("角色查询成功", roleService.getRolePage(query));
    }

    /**
     * 分页查询角色下可见的普通用户。
     *
     * @param query 角色ID及用户分页筛选条件
     * @return 已分配用户分页
     */
    @GetMapping("/userPage")
    @SystemCheckPermission("system:roleUser:list")
    public Result<PageDataVO<SysRoleUserVO>> getRoleUserPage(@Valid SysRoleUserQueryDTO query) {
        return Result.success("角色用户查询成功", roleService.getRoleUserPage(query));
    }

    /**
     * 分页查询尚未关联且可分配的正常用户。
     *
     * @param query 角色ID及用户分页筛选条件
     * @return 候选用户分页
     */
    @GetMapping("/availableUserPage")
    @SystemCheckPermission("system:roleUser:assign")
    public Result<PageDataVO<SysRoleUserVO>> getAvailableRoleUserPage(@Valid SysRoleUserQueryDTO query) {
        return Result.success("可分配用户查询成功", roleService.getAvailableRoleUserPage(query));
    }

    /**
     * 查询角色基础资料。
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:role:detail")
    public Result<SysRoleVO> getRoleDetail(@RequestParam Long id) {
        return Result.success("角色详情查询成功", roleService.getRoleDetail(id));
    }

    /**
     * 查询角色统一数据范围。
     *
     * @param id 角色ID
     * @return 数据范围详情
     */
    @GetMapping("/dataScope")
    @SystemCheckPermission("system:role:dataScope:list")
    public Result<SysRoleDataScopeVO> getRoleDataScope(@RequestParam Long id) {
        return Result.success("角色数据范围查询成功", roleService.getRoleDataScope(id));
    }

    /**
     * 新增角色基础资料。
     *
     * @param parameter 角色新增参数
     * @return 新增结果
     */
    @Log(title = "角色管理", type = LogType.INSERT, requestFields = {"roleName", "roleCode"})
    @PostMapping("/add")
    @SystemCheckPermission("system:role:create")
    public Result<Void> addRole(@Valid @RequestBody SysRoleAddDTO parameter) {
        roleService.addRole(parameter);
        return Result.success("新增角色成功");
    }

    /**
     * 修改角色基础资料。
     *
     * @param parameter 角色ID及基础资料
     * @return 修改结果
     */
    @Log(title = "角色管理", type = LogType.UPDATE, requestFields = {"id", "roleCode"})
    @PutMapping("/update")
    @SystemCheckPermission("system:role:update")
    public Result<Void> updateRole(@Valid @RequestBody SysRoleUpdateDTO parameter) {
        roleService.updateRole(parameter);
        return Result.success("修改角色成功");
    }

    /**
     * 修改角色状态。
     *
     * @param parameter 角色ID及目标状态
     * @return 状态变更结果
     */
    @Log(title = "角色管理", type = LogType.UPDATE, requestFields = {"id", "status"})
    @PutMapping("/updateStatus")
    @SystemCheckPermission("system:role:status")
    public Result<Void> updateRoleStatus(@Valid @RequestBody SysRoleStatusDTO parameter) {
        roleService.updateRoleStatus(parameter);
        return Result.success("修改角色状态成功");
    }

    /**
     * 分配角色统一数据范围。
     *
     * @param parameter 角色ID、范围及自定义部门
     * @return 数据范围保存结果
     */
    @Log(title = "角色数据范围", type = LogType.UPDATE, requestFields = {"id", "dataScope", "deptIds"})
    @PutMapping("/assignDataScope")
    @SystemCheckPermission("system:role:dataScope:assign")
    public Result<Void> assignRoleDataScope(@Valid @RequestBody SysRoleDataScopeDTO parameter) {
        roleService.assignRoleDataScope(parameter);
        return Result.success("角色数据范围保存成功");
    }

    /**
     * 分配明确勾选的角色菜单和按钮权限。
     *
     * @param parameter 角色ID及完整菜单权限集合
     * @return 权限分配结果
     */
    @Log(title = "角色权限", type = LogType.UPDATE, requestFields = {"id", "permissionIds"})
    @PutMapping("/assignPermissions")
    @SystemCheckPermission("system:rolePermission:assign")
    public Result<Void> assignRolePermissions(@Valid @RequestBody SysRolePermissionDTO parameter) {
        permissionService.assignRolePermissions(parameter);
        return Result.success("角色权限保存成功");
    }

    /**
     * 批量将用户加入角色，保留其其他角色。
     *
     * @param parameter 角色ID及本次加入的用户ID
     * @return 分配结果
     */
    @Log(title = "分配用户", type = LogType.UPDATE, requestFields = {"roleId", "userIds"})
    @PutMapping("/assignUsers")
    @SystemCheckPermission("system:roleUser:assign")
    public Result<Void> assignRoleUsers(@Valid @RequestBody SysRoleUserDTO parameter) {
        roleService.assignRoleUsers(parameter);
        return Result.success("分配用户成功");
    }

    /**
     * 批量移除当前角色的用户关联，保留其其他角色。
     *
     * @param parameter 角色ID及本次移除的用户ID
     * @return 移除结果
     */
    @Log(title = "移除用户", type = LogType.UPDATE, requestFields = {"roleId", "userIds"})
    @PutMapping("/unassignUsers")
    @SystemCheckPermission("system:roleUser:assign")
    public Result<Void> unassignRoleUsers(@Valid @RequestBody SysRoleUserDTO parameter) {
        roleService.unassignRoleUsers(parameter);
        return Result.success("移除用户成功");
    }

    /**
     * 删除没有用户关联的角色。
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @Log(title = "角色管理", type = LogType.DELETE, requestFields = "id")
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:role:delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return Result.success("删除角色成功");
    }
}
