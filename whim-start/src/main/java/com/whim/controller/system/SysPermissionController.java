package com.whim.controller.system;

import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.web.Result;
import com.whim.system.model.dto.sysPermission.SysPermissionQueryDTO;
import com.whim.system.model.vo.MenuVO;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/list")
    public Result<List<MenuVO>> getMenuList(SysPermissionQueryDTO sysPermissionQueryDTO) {
        return Result.success(sysPermissionService.getAllMenuThree(sysPermissionQueryDTO));
    }
}
