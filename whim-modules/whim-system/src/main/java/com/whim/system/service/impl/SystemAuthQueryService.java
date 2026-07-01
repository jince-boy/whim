package com.whim.system.service.impl;

import com.whim.satoken.constants.AuthUserType;
import com.whim.satoken.service.IAuthQueryService;
import com.whim.system.mapper.SysAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/07/01
 * @description 系统账号体系授权查询服务
 */
@Service
@RequiredArgsConstructor
public class SystemAuthQueryService implements IAuthQueryService {
    private final SysAuthMapper sysAuthMapper;

    /**
     * 判断是否支持系统账号体系。
     *
     * @param loginType Sa-Token 账号体系标识
     * @return true 表示支持系统账号体系
     */
    @Override
    public boolean supports(String loginType) {
        return AuthUserType.SYSTEM.equals(loginType);
    }

    /**
     * 获取用户权限标识集合。
     *
     * @param userId 用户ID
     * @return 权限标识集合
     */
    @Override
    public Set<String> getPermissionList(Long userId) {
        if (Objects.isNull(userId)) {
            return Set.of();
        }
        return toAuthCodeSet(sysAuthMapper.selectPermissionCodeListByUserId(userId));
    }

    /**
     * 获取用户角色编码集合。
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    @Override
    public Set<String> getRoleList(Long userId) {
        if (Objects.isNull(userId)) {
            return Set.of();
        }
        return toAuthCodeSet(sysAuthMapper.selectRoleCodeListByUserId(userId));
    }

    /**
     * 转换授权编码列表为有序去重集合。
     *
     * @param authCodeList 授权编码列表
     * @return 有序授权编码集合
     */
    private Set<String> toAuthCodeSet(List<String> authCodeList) {
        if (Objects.isNull(authCodeList) || authCodeList.isEmpty()) {
            return Set.of();
        }
        return authCodeList.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(authCode -> !authCode.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
