package com.whim.system.controller;


import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统权限菜单表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysPermission")
public class SysPermissionController {

    /**
     * 系统权限菜单表服务对象
     */
    private final ISysPermissionService sysPermissionService;
}

