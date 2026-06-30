package com.whim.system.controller;


import com.whim.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 操作日志控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysOperLog")
public class SysOperLogController {

    /**
     * 操作日志服务对象
     */
    private final ISysOperLogService sysOperLogService;
}

