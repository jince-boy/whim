package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.satoken.annotation.SystemCheckRole;
import com.whim.system.model.dto.permission.SysMenuQueryDTO;
import com.whim.system.model.dto.permission.SysMenuSaveDTO;
import com.whim.system.model.vo.permission.SysMenuTreeVO;
import com.whim.system.model.vo.permission.SysMenuVO;
import com.whim.system.model.vo.permission.SysRoleMenuTreeVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 菜单与权限统一维护接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/permission")
public class SysPermissionController {
    private final ISysPermissionService permissionService;

    /**
     * 根据id查询菜单详情。
     *
     * @param id 菜单id
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    @SystemCheckPermission("system:menu:detail")
    public Result<SysMenuVO> getMenuDetail(@PathVariable @Positive(message = "菜单ID必须大于0") Long id) {
        return Result.success("菜单详情查询成功", permissionService.getMenuDetail(id));
    }

    /**
     * 查询菜单列表，按名称筛选时保留祖先和匹配节点的子树。
     *
     * @param query 查询参数
     * @return 菜单树列表
     */
    @GetMapping
    @SystemCheckPermission("system:menu:list")
    public Result<List<SysMenuVO>> getMenuList(@Valid SysMenuQueryDTO query) {
        return Result.success("菜单列表查询成功", permissionService.getMenuList(query));
    }

    /**
     * 根据登录上下文中的菜单授权获取动态路由，不要求菜单配置权限标识。
     *
     * @return 保留隐藏页面、排除按钮和停用分支的路由菜单树
     */
    @GetMapping("/userMenus")
    public Result<List<SysMenuVO>> getUserMenus() {
        return Result.success("用户路由查询成功", permissionService.getUserMenus());
    }

    /**
     * 获取菜单下拉树列表。
     *
     * @return 包含按钮和停用节点的菜单树列表
     */
    @GetMapping("/tree")
    @SystemCheckPermission("system:menu:list")
    public Result<List<SysMenuTreeVO>> getMenuTreeList() {
        return Result.success("菜单树查询成功", permissionService.getMenuTreeList());
    }

    /**
     * 获取角色菜单授权树。
     *
     * @param roleId 角色id
     * @return 完整菜单树和角色已授权的菜单id
     */
    @GetMapping("/roleTree/{roleId}")
    @SystemCheckPermission("system:rolePermission:list")
    public Result<SysRoleMenuTreeVO> getRoleMenuTree(
            @PathVariable @Positive(message = "角色ID必须大于0") Long roleId) {
        return Result.success("角色菜单树查询成功", permissionService.getRoleMenuTree(roleId));
    }

    /**
     * 新增菜单权限，仅允许超级管理员操作。
     *
     * @param parameter 菜单新增参数
     * @return 新增菜单id
     */
    @PostMapping
    @SystemCheckRole("superadmin")
    @SystemCheckPermission("system:menu:create")
    public Result<Long> addMenu(@Valid @RequestBody SysMenuSaveDTO parameter) {
        return Result.success("菜单新增成功", permissionService.addMenu(parameter));
    }

    /**
     * 修改菜单权限。
     *
     * @param id 菜单id
     * @param parameter 菜单修改参数
     * @return 修改结果
     */
    @PutMapping("/{id}")
    @SystemCheckPermission("system:menu:update")
    public Result<Void> updateMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long id,
                                 @Valid @RequestBody SysMenuSaveDTO parameter) {
        permissionService.updateMenu(id, parameter);
        return Result.success("菜单修改成功");
    }

    /**
     * 根据菜单删除权限删除没有子节点的菜单，内置首页和核心菜单管理节点不能删除。
     *
     * @param id 菜单id
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SystemCheckPermission("system:menu:delete")
    public Result<Void> deleteMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long id) {
        permissionService.deleteMenu(id);
        return Result.success("菜单删除成功");
    }
}
