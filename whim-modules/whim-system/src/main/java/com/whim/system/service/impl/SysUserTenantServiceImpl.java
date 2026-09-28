package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.vo.tenant.MemberVO;
import com.whim.system.service.AuthorizationSessionInvalidator;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysUserTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户租户关联表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserTenantServiceImpl extends ServiceImpl<SysUserTenantMapper, SysUserTenant> implements ISysUserTenantService {
    private final ISysTenantService tenantService;
    private final AuthorizationSessionInvalidator sessionInvalidator;

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 租户ID集合
     */
    @Override
    public Set<Long> getTenantIdsByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return Set.of();
        }
        return Objects.requireNonNullElse(baseMapper.selectTenantIdsByUserId(userId), Set.of());
    }

    /**
     * 查询用户当前可访问的租户。
     *
     * @param userId 用户ID
     * @return 可访问租户列表
     */
    @Override
    public List<SysTenant> getTenantListByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return List.of();
        }
        return Objects.requireNonNullElse(baseMapper.selectTenantListByUserId(userId), List.of());
    }

    /** 查询当前租户已有成员。 */
    @Override
    public List<MemberVO> listMembers() {
        return baseMapper.selectMemberList(tenantService.getRequiredCurrentTenant().getId());
    }

    /** 修改当前租户成员状态并在提交后撤销旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMemberStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysTenant tenant = tenantService.getRequiredCurrentTenant();
        if (status == 1 && Objects.equals(tenant.getUserId(), userId)) {
            throw new IllegalArgumentException("不能停用租户管理员成员关系");
        }
        SysUserTenant member = lambdaQuery().eq(SysUserTenant::getUserId, userId)
                .eq(SysUserTenant::getTenantId, tenant.getId()).one();
        if (member == null) {
            throw new TenantAccessDeniedException("目标用户不是当前租户成员");
        }
        member.setStatus(status);
        updateById(member);
        sessionInvalidator.kickoutAfterCommit(Set.of(userId));
    }
}

