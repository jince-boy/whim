package com.whim.system.controller;


import com.whim.system.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户角色关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysUserRole")
public class SysUserRoleController {

    /**
     * 系统用户角色关联表服务对象
     */
    private final ISysUserRoleService sysUserRoleService;
}

