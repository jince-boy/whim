package com.whim.controller.system;


import com.whim.system.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色部门关联表控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sysRoleDept")
public class SysRoleDeptController {

    /**
     * 系统角色部门关联表服务对象
     */
    private final ISysRoleDeptService sysRoleDeptService;
}

