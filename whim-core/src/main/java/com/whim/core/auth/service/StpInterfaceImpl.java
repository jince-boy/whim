package com.whim.core.auth.service;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Jince
 * date: 2024/10/24 23:27
 * description: saToken 权限认证
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StpInterfaceImpl implements StpInterface {
    private final ISysPermissionService sysPermissionService;
    private final ISysRoleService sysRoleService;

    @Override
    public List<String> getPermissionList(Object userId, String loginType) {
        long sysUserId = Long.parseLong(userId.toString());
        if (sysUserId == 1L) {
            return List.of("*");
        }
        return sysPermissionService.getPermissionCodeByUserId(sysUserId);
    }

    @Override
    public List<String> getRoleList(Object userId, String s) {
        return sysRoleService.getRoleCodeByUserId((Long) userId);
    }
}
