package com.whim.controller;


import com.whim.common.base.BaseController;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * date 2024-10-23 19:49:04
 * description: 系统用户(SysUser)表控制层
 */
@RestController
@RequestMapping("/sysUser")
@RequiredArgsConstructor
public class SysUserController extends BaseController {
    /**
     * 服务对象
     */
    private final ISysUserService sysUserService;

}

