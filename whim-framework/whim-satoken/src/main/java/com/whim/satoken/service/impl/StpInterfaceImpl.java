package com.whim.satoken.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.satoken.service.IAuthQueryService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Jince
 * @date 2026/07/01
 * @description Sa-Token 权限验证适配器
 */
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final List<IAuthQueryService> authQueryServices;

    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = (Long) loginId;
        for (IAuthQueryService authQueryService : authQueryServices) {
            if (authQueryService.supports(loginType)) {
                return List.copyOf(authQueryService.getPermissionList(userId));
            }
        }
        return List.of();
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = (Long) loginId;
        for (IAuthQueryService authQueryService : authQueryServices) {
            if (authQueryService.supports(loginType)) {
                return List.copyOf(authQueryService.getRoleList(userId));
            }
        }
        return List.of();
    }
}
