package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.vo.tenant.MemberVO;
import com.whim.system.service.ISysTenantService;
import com.whim.system.service.ISysDeptService;
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
    private final ISysDeptService deptService;
    private final AuthenticationSession authenticationSession;
    private final SysDataScopeService dataScopeService;

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
        return baseMapper.selectTenantIdsByUserId(userId);
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
        return baseMapper.selectTenantListByUserId(userId);
    }

    /** 按本次列表授权角色的数据范围查询当前租户成员。 */
    @Override
    public List<MemberVO> listMembers() {
        return baseMapper.selectMemberList(dataScopeService.resolveCurrentTenantDataScope("system:member:list"));
    }

    /** 使用目标成员主部门或本人归属校验本次读取范围。 */
    @Override
    public SysUserTenant getRequiredMemberInDataScope(Long userId, DataScopeDecisionDTO scope) {
        SysUserTenant member = getOne(scopedMemberQuery(scope).eq(SysUserTenant::getUserId, userId));
        if (member == null) {
            throw new TenantAccessDeniedException("目标成员不在本次操作范围内");
        }
        return member;
    }

    /** 写入前锁定目标成员，保持归属校验到事务提交。 */
    @Override
    public SysUserTenant lockRequiredMemberInDataScope(Long userId, DataScopeDecisionDTO scope) {
        SysUserTenant member = getOne(scopedMemberQuery(scope)
                .eq(SysUserTenant::getUserId, userId).last("FOR UPDATE"));
        if (member == null) {
            throw new TenantAccessDeniedException("目标成员不在本次操作范围内");
        }
        return member;
    }

    /** 岗位成员关系查询不得泄露范围外成员的用户 ID。 */
    @Override
    public void requireMembersInDataScope(Set<Long> userIds, DataScopeDecisionDTO scope) {
        if (userIds.isEmpty() || scope.isAll()) {
            return;
        }
        long visibleCount = count(scopedMemberQuery(scope).in(SysUserTenant::getUserId, userIds));
        if (visibleCount != userIds.size()) {
            throw new TenantAccessDeniedException("岗位包含本次操作范围外的成员");
        }
    }

    /** 批量覆盖前锁定成员归属，范围外的已有或新增成员使整批操作失败。 */
    @Override
    public List<SysUserTenant> lockMembersInDataScope(Set<Long> userIds, DataScopeDecisionDTO scope) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        List<SysUserTenant> members = list(scopedMemberQuery(scope)
                .in(SysUserTenant::getUserId, userIds).last("FOR UPDATE"));
        if (!scope.isAll() && members.size() != userIds.size()) {
            throw new TenantAccessDeniedException("岗位成员不在本次操作范围内");
        }
        return members;
    }

    /** 查询有效成员在指定租户的有效主部门ID。 */
    @Override
    public Long getActiveDepartmentId(Long userId, Long tenantId) {
        Long deptId = baseMapper.selectActiveDepartmentId(userId, tenantId);
        if (deptId == null) {
            return null;
        }
        try {
            deptService.getRequiredActiveDepartment(deptId, tenantId);
            return deptId;
        } catch (TenantAccessDeniedException exception) {
            return null;
        }
    }

    /** 修改当前租户成员的主部门并在事务提交后撤销旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMemberDepartment(Long userId, Long deptId) {
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:member:department");
        Long tenantId = scope.getTenantId();
        SysUserTenant member = getOne(scopedMemberQuery(scope).eq(SysUserTenant::getUserId, userId));
        if (member == null) {
            throw new TenantAccessDeniedException("目标成员不在本次操作范围内");
        }
        if (deptId != null) {
            deptService.getRequiredActiveDepartment(deptId, tenantId);
            if (!scope.isAll() && !scope.getDeptIds().contains(deptId)) {
                throw new TenantAccessDeniedException("目标部门不在本次操作范围内");
            }
        }
        if (!update(new SysUserTenant(), scopedMemberUpdate(member.getId(), userId, scope)
                .set(SysUserTenant::getDeptId, deptId))) {
            throw new TenantAccessDeniedException("目标成员已不可操作");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 修改当前租户成员状态并在提交后撤销旧会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMemberStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:member:status");
        SysTenant tenant = tenantService.getRequiredCurrentTenant();
        if (status == 1 && Objects.equals(tenant.getUserId(), userId)) {
            throw new IllegalArgumentException("不能停用租户管理员成员关系");
        }
        SysUserTenant member = getOne(scopedMemberQuery(scope).eq(SysUserTenant::getUserId, userId));
        if (member == null) {
            throw new TenantAccessDeniedException("目标成员不在本次操作范围内");
        }
        SysUserTenant changes = new SysUserTenant();
        changes.setStatus(status);
        if (!update(changes, scopedMemberUpdate(member.getId(), userId, scope))) {
            throw new TenantAccessDeniedException("目标成员已不可操作");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, Set.of(userId));
    }

    /** 为单个或一组成员查询同时约束租户和本次动作的数据范围。 */
    private LambdaQueryWrapper<SysUserTenant> scopedMemberQuery(DataScopeDecisionDTO scope) {
        LambdaQueryWrapper<SysUserTenant> query = Wrappers.<SysUserTenant>lambdaQuery()
                .eq(SysUserTenant::getTenantId, scope.getTenantId());
        if (scope.isAll()) {
            return query;
        }
        if (scope.getDeptIds().isEmpty()) {
            return query.eq(SysUserTenant::getUserId, scope.getUserId());
        }
        if (scope.isSelf()) {
            return query.and(condition -> condition.in(SysUserTenant::getDeptId, scope.getDeptIds())
                    .or().eq(SysUserTenant::getUserId, scope.getUserId()));
        }
        return query.in(SysUserTenant::getDeptId, scope.getDeptIds());
    }

    /** 最终写入再次约束成员目标，防止校验后越过租户或部门范围。 */
    private LambdaUpdateWrapper<SysUserTenant> scopedMemberUpdate(Long memberId, Long userId,
                                                                   DataScopeDecisionDTO scope) {
        LambdaUpdateWrapper<SysUserTenant> update = Wrappers.<SysUserTenant>lambdaUpdate()
                .eq(SysUserTenant::getId, memberId).eq(SysUserTenant::getUserId, userId)
                .eq(SysUserTenant::getTenantId, scope.getTenantId());
        if (scope.isAll()) {
            return update;
        }
        if (scope.getDeptIds().isEmpty()) {
            return update.eq(SysUserTenant::getUserId, scope.getUserId());
        }
        if (scope.isSelf()) {
            return update.and(condition -> condition.in(SysUserTenant::getDeptId, scope.getDeptIds())
                    .or().eq(SysUserTenant::getUserId, scope.getUserId()));
        }
        return update.in(SysUserTenant::getDeptId, scope.getDeptIds());
    }
}

