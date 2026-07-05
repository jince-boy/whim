package com.whim.controller.system;


import com.whim.system.service.ISysTenantPackagePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户套餐权限关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysTenantPackagePermission")
public class SysTenantPackagePermissionController {

    /**
     * 系统租户套餐权限关联表服务对象
     */
    private final ISysTenantPackagePermissionService sysTenantPackagePermissionService;
}

