package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysTenantMapper;
import com.whim.system.mapper.SysTenantPackageMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysTenantPackage;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.service.ISysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统租户表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {
    private final AuthenticationContext authenticationContext;
    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysTenantPackageMapper tenantPackageMapper;
    private final AuthenticationSession authenticationSession;

    /**
     * 查询全部当前可用租户。
     *
     * @return 可用租户列表
     */
    @Override
    public List<SysTenant> getAvailableTenantList() {
        return baseMapper.selectAvailableTenantList();
    }

    /**
     * 查询全部当前可用租户ID。
     *
     * @return 可用租户ID集合
     */
    @Override
    public Set<Long> getAvailableTenantIds() {
        return baseMapper.selectAvailableTenantIds();
    }

    /** 获取当前可用租户并检查当前账号的成员资格。 */
    @Override
    public SysTenant getRequiredCurrentTenant() {
        Long userId = authenticationContext.getUserId();
        if (!isActiveUser(userId)) {
            throw new TenantAccessDeniedException("当前用户不可用");
        }
        Long tenantId = authenticationContext.getTenantId();
        if (tenantId == null) {
            throw new TenantAccessDeniedException("请先选择租户");
        }
        SysTenant tenant = baseMapper.selectActiveTenantById(tenantId);
        if (tenant == null) {
            throw new TenantAccessDeniedException("当前租户不可用");
        }
        if (!authenticationContext.isSuperAdministrator()
                && userTenantMapper.selectOne(Wrappers.<SysUserTenant>lambdaQuery()
                        .select(SysUserTenant::getId)
                        .eq(SysUserTenant::getUserId, userId)
                        .eq(SysUserTenant::getTenantId, tenantId)
                        .eq(SysUserTenant::getStatus, 0)) == null) {
            throw new TenantAccessDeniedException("当前租户成员不可用");
        }
        return tenant;
    }

    /** 检查当前账号的全局平台管理员资格。 */
    @Override
    public void requirePlatformAdministrator() {
        if (!isActiveUser(authenticationContext.getUserId()) || !authenticationContext.isSuperAdministrator()) {
            throw new TenantAccessDeniedException("无权执行平台管理操作");
        }
    }

    /** 修改租户状态并在提交后撤销成员的旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTenantStatus(Long tenantId, Integer status) {
        requirePlatformAdministrator();
        validateStatus(status);
        SysTenant tenant = requireExistingTenant(tenantId);
        tenant.setStatus(status);
        updateById(tenant);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, getMemberUserIds(tenantId));
    }

    /** 修改租户套餐并在提交后撤销成员的旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTenantPackage(Long tenantId, Long packageId) {
        requirePlatformAdministrator();
        SysTenant tenant = requireExistingTenant(tenantId);
        SysTenantPackage tenantPackage = packageId == null ? null : tenantPackageMapper.selectById(packageId);
        if (tenantPackage == null || tenantPackage.getStatus() != 0) {
            throw new TenantAccessDeniedException("租户套餐不可用");
        }
        tenant.setPackageId(packageId);
        updateById(tenant);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, getMemberUserIds(tenantId));
    }

    /** 查询未删除的目标租户。 */
    private SysTenant requireExistingTenant(Long tenantId) {
        SysTenant tenant = getById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("目标租户不存在");
        }
        return tenant;
    }

    /** 判断用户当前是否启用。 */
    private boolean isActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return user != null && user.getStatus() == 0;
    }

    /** 查询租户内未删除成员的用户ID。 */
    private List<Long> getMemberUserIds(Long tenantId) {
        return userTenantMapper.selectObjs(Wrappers.<SysUserTenant>lambdaQuery()
                        .select(SysUserTenant::getUserId)
                        .eq(SysUserTenant::getTenantId, tenantId))
                .stream().map(Long.class::cast).toList();
    }

    /** 检查启停状态。 */
    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
    }
}

