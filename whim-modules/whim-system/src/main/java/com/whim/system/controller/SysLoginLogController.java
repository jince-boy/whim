package com.whim.system.controller;


import com.whim.system.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统登录日志控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysLoginLog")
public class SysLoginLogController {

    /**
     * 系统登录日志服务对象
     */
    private final ISysLoginLogService sysLoginLogService;
}

