package com.whim.controller.system;


import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.permission.PermissionStatusDTO;
import com.whim.system.model.vo.permission.PermissionVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统权限菜单表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController {

    /**
     * 系统权限菜单表服务对象
     */
    private final ISysPermissionService sysPermissionService;

    /** 查询系统权限目录。 */
    @GetMapping
    @SystemCheckPermission("system:permission:list")
    public Result<List<PermissionVO>> listAssignablePermissions() {
        return Result.success("权限查询成功", sysPermissionService.listPermissions());
    }

    /** 修改全局权限状态。 */
    @PutMapping("/{permissionId}/status")
    @SystemCheckPermission("system:permission:status")
    public Result<Void> setPermissionStatus(@PathVariable Long permissionId,
                                            @RequestBody @Valid PermissionStatusDTO request) {
        sysPermissionService.setPermissionStatus(permissionId, request.getStatus());
        return Result.success("权限状态修改成功");
    }
}

