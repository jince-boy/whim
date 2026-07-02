package com.whim.system.controller;


import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysUser")
public class SysUserController {

    /**
     * 系统用户表服务对象
     */
    private final ISysUserService sysUserService;
}

