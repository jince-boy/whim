package com.whim.system.controller;


import com.whim.system.service.ISysRolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色-权限关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysRolePermission")
public class SysRolePermissionController {

    /**
     * 角色-权限关联表服务对象
     */
    private final ISysRolePermissionService sysRolePermissionService;
}

