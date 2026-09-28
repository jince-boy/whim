package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysTenantPackageMapper;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.entity.SysTenantPackage;
import com.whim.system.service.AuthorizationSessionInvalidator;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysTenantPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统租户套餐表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysTenantPackageServiceImpl extends ServiceImpl<SysTenantPackageMapper, SysTenantPackage> implements ISysTenantPackageService {
    private final ISysTenantService tenantService;
    private final SysUserTenantMapper userTenantMapper;
    private final AuthorizationSessionInvalidator sessionInvalidator;

    /** 修改套餐状态并在提交后撤销受影响成员的旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPackageStatus(Long packageId, Integer status) {
        tenantService.requirePlatformAdministrator();
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysTenantPackage tenantPackage = getById(packageId);
        if (tenantPackage == null) {
            throw new IllegalArgumentException("目标套餐不存在");
        }
        tenantPackage.setStatus(status);
        updateById(tenantPackage);
        sessionInvalidator.kickoutAfterCommit(userTenantMapper.selectMemberUserIdsByPackageId(packageId));
    }
}

