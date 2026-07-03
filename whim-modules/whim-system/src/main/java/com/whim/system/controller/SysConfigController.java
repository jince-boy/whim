package com.whim.system.controller;


import com.whim.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/03
 * @description 系统配置表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysConfig")
public class SysConfigController {

    /**
     * 系统配置表服务对象
     */
    private final ISysConfigService sysConfigService;
}

