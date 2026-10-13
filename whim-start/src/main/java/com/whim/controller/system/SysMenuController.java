package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.menu.MenuSaveDTO;
import com.whim.system.model.vo.menu.MenuVO;
import com.whim.system.service.ISysMenuService;
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
 * @description 当前用户导航及系统菜单管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/menu")
public class SysMenuController {
    private final ISysMenuService menuService;

    /** 当前用户导航只要求登录，不要求拥有菜单管理权限。 */
    @GetMapping("/userMenus")
    @SystemCheckLogin
    public Result<List<MenuVO>> userMenus() {
        return Result.success("用户菜单获取成功", menuService.getCurrentMenus());
    }

    /** 查询菜单管理树，包含目录、页面、按钮和外链。 */
    @GetMapping
    @SystemCheckPermission("system:menu:list")
    public Result<List<MenuVO>> listMenus(@RequestParam(required = false) String menuName) {
        return Result.success("菜单查询成功", menuService.listMenus(menuName));
    }

    /** 获取单个节点的完整编辑数据。 */
    @GetMapping("/{menuId}")
    @SystemCheckPermission("system:menu:detail")
    public Result<MenuVO> getMenu(@PathVariable Long menuId) {
        return Result.success("菜单详情获取成功", menuService.getMenu(menuId));
    }

    /** 创建菜单或自定义操作权限。 */
    @PostMapping
    @SystemCheckPermission("system:menu:create")
    public Result<Long> createMenu(@RequestBody @Valid MenuSaveDTO request) {
        return Result.success("菜单创建成功", menuService.createMenu(request));
    }

    /** 修改完整菜单配置，授权变更在提交后撤销受影响会话。 */
    @PutMapping("/{menuId}")
    @SystemCheckPermission("system:menu:update")
    public Result<Void> updateMenu(@PathVariable Long menuId, @RequestBody @Valid MenuSaveDTO request) {
        menuService.updateMenu(menuId, request);
        return Result.success("菜单修改成功");
    }

    /** 删除无子节点、无授权引用的自定义节点。 */
    @DeleteMapping("/{menuId}")
    @SystemCheckPermission("system:menu:delete")
    public Result<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return Result.success("菜单删除成功");
    }
}
