package com.whim.controller.system;


import com.whim.common.base.BaseController;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * date 2024-10-23 19:54:24
 * description: 系统角色(SysRole)表控制层
 */
@RestController
@RequestMapping("/system/sysRole")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {
    /**
     * 服务对象
     */
    private final ISysRoleService sysRoleService;

}

