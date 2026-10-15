package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.satoken.annotation.SystemCheckRole;
import com.whim.system.model.dto.menu.MenuSaveDTO;
import com.whim.system.model.dto.permission.PermissionStatusDTO;
import com.whim.system.model.vo.menu.MenuVO;
import com.whim.system.model.vo.permission.PermissionVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 统一提供菜单、按钮权限及当前用户导航管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController {
    private final ISysPermissionService permissionService;

    /** 当前用户导航只要求登录，不要求拥有菜单管理权限。 */
    @GetMapping("/userMenus")
    @SystemCheckLogin
    public Result<List<MenuVO>> userMenus() {
        return Result.success("用户菜单获取成功", permissionService.getCurrentMenus());
    }

    /** 查询菜单管理树，包含目录、页面、按钮和外链。 */
    @GetMapping
    @SystemCheckPermission("system:menu:list")
    public Result<List<MenuVO>> listMenus(@RequestParam(required = false) String menuName) {
        return Result.success("菜单查询成功", permissionService.listMenus(menuName));
    }

    /** 查询角色授权使用的扁平权限选项。 */
    @GetMapping("/options")
    @SystemCheckPermission("system:permission:list")
    public Result<List<PermissionVO>> listAssignablePermissions() {
        return Result.success("权限查询成功", permissionService.listPermissions());
    }

    /** 获取单个节点的完整编辑数据。 */
    @GetMapping("/{permissionId}")
    @SystemCheckPermission("system:menu:detail")
    public Result<MenuVO> getMenu(@PathVariable Long permissionId) {
        return Result.success("菜单详情获取成功", permissionService.getMenu(permissionId));
    }

    /** 创建菜单或自定义操作权限。 */
    @PostMapping
    @SystemCheckPermission("system:menu:create")
    @SystemCheckRole("superadmin")
    public Result<Long> createMenu(@RequestBody @Valid MenuSaveDTO request) {
        return Result.success("菜单创建成功", permissionService.createMenu(request));
    }

    /** 修改完整菜单配置，授权变更在提交后撤销受影响会话。 */
    @PutMapping("/{permissionId}")
    @SystemCheckPermission("system:menu:update")
    public Result<Void> updateMenu(@PathVariable Long permissionId, @RequestBody @Valid MenuSaveDTO request) {
        permissionService.updateMenu(permissionId, request);
        return Result.success("菜单修改成功");
    }

    /** 修改全局权限状态。 */
    @PutMapping("/{permissionId}/status")
    @SystemCheckPermission("system:permission:status")
    public Result<Void> setPermissionStatus(@PathVariable Long permissionId,
                                          @RequestBody @Valid PermissionStatusDTO request) {
        permissionService.setPermissionStatus(permissionId, request.getStatus());
        return Result.success("权限状态修改成功");
    }

    /** 删除叶子节点并同步清理角色授权，核心管理节点除外。 */
    @DeleteMapping("/{permissionId}")
    @SystemCheckPermission("system:menu:delete")
    @SystemCheckRole("superadmin")
    public Result<Void> deleteMenu(@PathVariable Long permissionId) {
        permissionService.deleteMenu(permissionId);
        return Result.success("菜单删除成功");
    }
}
