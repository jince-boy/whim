package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysUser;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysUserService;
import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;
    private final ISysUserTenantService sysUserTenantService;
    private final ISysTenantService sysTenantService;
    private final AuthenticationSession authenticationSession;

    /**
     * 根据用户名查询未删除用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 可访问租户ID集合
     */
    @Override
    public Set<Long> getAccessibleTenantIds(Long userId) {
        if (sysRoleService.isSuperAdministrator(userId)) {
            return sysTenantService.getAvailableTenantIds();
        }
        return sysUserTenantService.getTenantIdsByUserId(userId);
    }

    /**
     * 构建系统账号认证上下文。
     *
     * @param user 用户实体
     * @return 用户认证信息
     */
    @Override
    public UserInfo buildUserInfo(SysUser user) {
        Set<Long> tenantIds = getAccessibleTenantIds(user.getId());
        Long currentTenantId = resolveInitialTenantId(user.getDefaultTenantId(), tenantIds);
        return buildUserInfo(user, tenantIds, currentTenantId);
    }

    /**
     * 按指定当前租户构建系统账号认证上下文。
     *
     * @param user            用户实体
     * @param currentTenantId 当前租户ID，平台上下文时为空
     * @return 用户认证信息
     */
    @Override
    public UserInfo buildUserInfo(SysUser user, Long currentTenantId) {
        Set<Long> tenantIds = getAccessibleTenantIds(user.getId());
        if (currentTenantId != null && !tenantIds.contains(currentTenantId)) {
            throw new TenantAccessDeniedException("无权访问该租户或租户不可用");
        }
        return buildUserInfo(user, tenantIds, currentTenantId);
    }

    /**
     * 修改全局用户状态并在提交后撤销其旧会话。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserStatus(Long userId, Integer status) {
        sysTenantService.requirePlatformAdministrator();
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("目标用户不存在");
        }
        if (status == 1 && sysRoleService.isSuperAdministrator(userId)) {
            throw new IllegalArgumentException("不能停用平台超级管理员");
        }
        user.setStatus(status);
        updateById(user);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /**
     * 使用已经确认的租户范围构建用户认证上下文。
     *
     * @param user            用户实体
     * @param tenantIds       可访问租户ID集合
     * @param currentTenantId 当前租户ID
     * @return 用户认证信息
     */
    private UserInfo buildUserInfo(SysUser user, Set<Long> tenantIds, Long currentTenantId) {

        UserInfo userInfo = BeanConvertUtils.convert(user, UserInfo.class);
        userInfo.setTenantIds(new LinkedHashSet<>(tenantIds));
        if (user.getDefaultTenantId() != null && tenantIds.contains(user.getDefaultTenantId())) {
            userInfo.setDefaultTenantId(user.getDefaultTenantId());
        }
        userInfo.setCurrentTenantId(currentTenantId);
        userInfo.setLoginType(AuthUserType.SYSTEM);
        List<RoleInfo> activeRoleList = sysRoleService.getRoleInfoListByUserIdAndTenantId(user.getId(), currentTenantId);
        List<RoleInfo> roleInfoList = new ArrayList<>();
        Set<Long> roleIds = new LinkedHashSet<>();
        Set<String> roleCodeSet = new LinkedHashSet<>();
        for (RoleInfo roleInfo : activeRoleList) {
            roleIds.add(roleInfo.getRoleId());
            if (!roleInfo.getRoleCode().isEmpty()) {
                roleInfoList.add(roleInfo);
                roleCodeSet.add(roleInfo.getRoleCode());
            }
        }
        userInfo.setPermissionCodeSet(
                sysPermissionService.getPermissionCodeSetByUserIdAndTenantId(user.getId(), currentTenantId, roleIds)
        );
        userInfo.setRoleCodeSet(roleCodeSet);
        userInfo.setRoleInfoList(roleInfoList);
        return userInfo;
    }

    /**
     * 根据默认租户和可访问租户数量确定登录后的初始租户。
     *
     * @param defaultTenantId 默认租户ID
     * @param tenantIds       可访问租户ID集合
     * @return 初始租户ID，多租户且未配置有效默认租户时为空
     */
    private Long resolveInitialTenantId(Long defaultTenantId, Set<Long> tenantIds) {
        if (defaultTenantId != null && tenantIds.contains(defaultTenantId)) {
            return defaultTenantId;
        }
        if (tenantIds.size() == 1) {
            return tenantIds.iterator().next();
        }
        return null;
    }
}

