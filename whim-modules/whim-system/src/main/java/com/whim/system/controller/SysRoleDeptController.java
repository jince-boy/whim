package com.whim.system.controller;


import com.whim.system.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色与部门关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysRoleDept")
public class SysRoleDeptController {

    /**
     * 角色与部门关联表服务对象
     */
    private final ISysRoleDeptService sysRoleDeptService;
}

