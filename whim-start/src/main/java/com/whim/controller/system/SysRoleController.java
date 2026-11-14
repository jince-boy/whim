package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.role.SysRoleAddDTO;
import com.whim.system.model.dto.role.SysRoleDataScopeDTO;
import com.whim.system.model.dto.role.SysRolePermissionDTO;
import com.whim.system.model.dto.role.SysRoleQueryDTO;
import com.whim.system.model.dto.role.SysRoleStatusDTO;
import com.whim.system.model.dto.role.SysRoleUpdateDTO;
import com.whim.system.model.vo.role.SysRoleDataScopeVO;
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
 * @description 角色管理接口，沿用菜单权限码控制各项操作。
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
    @PutMapping("/assignPermissions")
    @SystemCheckPermission("system:rolePermission:assign")
    public Result<Void> assignRolePermissions(@Valid @RequestBody SysRolePermissionDTO parameter) {
        permissionService.assignRolePermissions(parameter);
        return Result.success("角色权限保存成功");
    }

    /**
     * 删除没有用户关联的角色。
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:role:delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return Result.success("删除角色成功");
    }
}
