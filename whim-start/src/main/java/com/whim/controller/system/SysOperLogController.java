package com.whim.controller.system;


import com.whim.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志(SysOperLog)表控制层
 *
 * @author Jince
 * @since 2025-08-19 21:46:45
 */
@RestController
@RequestMapping("/sysOperLog")
@RequiredArgsConstructor
public class SysOperLogController {
    private final ISysOperLogService sysOperLogService;

}

