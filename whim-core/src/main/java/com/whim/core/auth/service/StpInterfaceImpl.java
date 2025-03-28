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
        // 后台用户权限认证
        if (loginType.equals("admin")) {
            long sysUserId = Long.parseLong(userId.toString());
            if (sysUserId == 1L) {
                return List.of("*");
            }
            return sysPermissionService.getPermissionCodeByUserId(sysUserId);
        }
        // 此处可根据用户类型进行权限获取
        if (loginType.equals("user")) {
            return List.of();
        }
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object userId, String loginType) {
        // 后台用户权限认证
        if (loginType.equals("admin")) {
            return sysRoleService.getRoleCodeByUserId((Long) userId);
        }
        // 此处可根据用户类型进行权限获取
        if (loginType.equals("user")) {
            return List.of();
        }
        return List.of();

    }
}
