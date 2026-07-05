package com.whim.controller.system;


import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysTenant")
public class SysTenantController {

    /**
     * 系统租户表服务对象
     */
    private final ISysTenantService sysTenantService;
}

