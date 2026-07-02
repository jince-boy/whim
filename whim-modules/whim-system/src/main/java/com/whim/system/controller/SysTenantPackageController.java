package com.whim.system.controller;


import com.whim.system.service.ISysTenantPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户套餐表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysTenantPackage")
public class SysTenantPackageController {

    /**
     * 系统租户套餐表服务对象
     */
    private final ISysTenantPackageService sysTenantPackageService;
}

