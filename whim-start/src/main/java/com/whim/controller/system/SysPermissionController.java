package com.whim.controller.system;

import com.whim.core.web.Result;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysPermission.SysPermissionInsertDTO;
import com.whim.system.model.dto.sysPermission.SysPermissionQueryDTO;
import com.whim.system.model.vo.MenuVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.web.annotation.SystemApiPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jince
 * date: 2025/9/9 16:20
 * description: 菜单管理
 */
@SystemApiPrefix
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysPermissionController {
    private final ISysPermissionService sysPermissionService;


    /**
     * 获取菜单列表
     *
     * @param sysPermissionQueryDTO 查询参数
     * @return 菜单列表
     */
    @SystemCheckPermission("system:menu:query")
    @GetMapping("/list")
    public Result<List<MenuVO>> getMenuList(SysPermissionQueryDTO sysPermissionQueryDTO) {
        return Result.success(sysPermissionService.getAllMenuThree(sysPermissionQueryDTO));
    }

    /**
     * 添加菜单
     *
     * @param sysPermissionInsertDTO 菜单信息
     * @return 添加结果
     */
    @SystemCheckPermission("system:menu:add")
    @PostMapping("/add")
    public Result<Void> addMenu(@RequestBody SysPermissionInsertDTO sysPermissionInsertDTO) {
        if (sysPermissionService.insertPermission(sysPermissionInsertDTO)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }
}
