package com.whim.controller;


import com.whim.common.base.BaseController;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * date 2024-10-23 19:54:24
 * description: 菜单权限(SysPermission)表控制层
 */
@RestController
@RequestMapping("/sysPermission")
@RequiredArgsConstructor
public class SysPermissionController extends BaseController {
    private final ISysPermissionService sysPermissionService;

}

