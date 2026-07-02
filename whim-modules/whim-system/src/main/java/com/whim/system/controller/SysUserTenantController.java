package com.whim.system.controller;


import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户租户关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysUserTenant")
public class SysUserTenantController {

    /**
     * 系统用户租户关联表服务对象
     */
    private final ISysUserTenantService sysUserTenantService;
}

