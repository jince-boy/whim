package com.whim.satoken.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.whim.satoken.service.IAuthQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Jince
 * @date 2026/07/01
 * @description Sa-Token 权限验证适配器，通过业务模块提供的授权查询扩展点加载角色与权限。
 */
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final ObjectProvider<IAuthQueryService> authQueryServices;

    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return queryAuthCodeList(loginId, loginType, IAuthQueryService::getPermissionList);
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
        return queryAuthCodeList(loginId, loginType, IAuthQueryService::getRoleList);
    }

    /**
     * 查询指定账号体系下的授权编码列表。
     *
     * @param loginId       登录账号ID
     * @param loginType     登录账号体系
     * @param queryFunction 授权查询函数
     * @return 授权编码列表
     */
    private List<String> queryAuthCodeList(Object loginId, String loginType,
                                           Function<IAuthQueryService, Set<String>> queryFunction) {
        Long userId = parseUserId(loginId);
        if (Objects.isNull(userId)) {
            return List.of();
        }
        return authQueryServices.orderedStream()
                .filter(authQueryService -> authQueryService.supports(loginType))
                .findFirst()
                .map(authQueryService -> queryFunction.apply(authQueryService))
                .map(this::normalizeAuthCodeList)
                .orElseGet(List::of);
    }

    /**
     * 解析 Sa-Token 登录账号ID。
     *
     * @param loginId 登录账号ID
     * @return 用户ID，解析失败时返回 null
     */
    private Long parseUserId(Object loginId) {
        if (Objects.isNull(loginId)) {
            return null;
        }
        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * 规整授权编码集合，过滤空值并去重。
     *
     * @param authCodeSet 授权编码集合
     * @return 授权编码列表
     */
    private List<String> normalizeAuthCodeList(Set<String> authCodeSet) {
        if (Objects.isNull(authCodeSet) || authCodeSet.isEmpty()) {
            return List.of();
        }
        return authCodeSet.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(authCode -> !authCode.isEmpty())
                .distinct()
                .toList();
    }
}
