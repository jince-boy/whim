package com.whim.core.auth.service;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.core.auth.provider.IAccountAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * date: 2024/10/24 23:27
 * description: saToken 自定义权限加载接口实现类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StpInterfaceImpl implements StpInterface {
    private final Map<String, IAccountAuthProvider> permissionProvider;

    @Override
    public List<String> getPermissionList(Object userId, String loginType) {
        // 后台用户权限认证
        if (loginType.equals("system")) {
            long sysUserId = Long.parseLong(userId.toString());
            if (sysUserId == 1L) {
                return List.of("*");
            }
            return permissionProvider.get("system").getPermissionList(sysUserId);
        }
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object userId, String loginType) {
        // 后台用户权限认证
        if (loginType.equals("system")) {
            return permissionProvider.get("system").getRoleList((Long) userId);
        }
        return List.of();

    }
}
