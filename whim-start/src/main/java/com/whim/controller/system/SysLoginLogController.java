package com.whim.controller.system;


import com.whim.system.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统登录日志(SysLoginLog)表控制层
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@RestController
@RequestMapping("/sysLoginLog")
@RequiredArgsConstructor
public class SysLoginLogController{
    /**
     * 服务对象
     */
    private final ISysLoginLogService sysLoginLogService;

}

