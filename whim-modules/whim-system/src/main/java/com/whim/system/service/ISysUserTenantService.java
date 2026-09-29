package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.vo.tenant.MemberVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户租户关联表服务接口
 */
public interface ISysUserTenantService extends IService<SysUserTenant> {

    /**
     * 查询用户当前可访问的租户ID集合。
     *
     * @param userId 用户ID
     * @return 租户ID集合
     */
    Set<Long> getTenantIdsByUserId(Long userId);

    /**
     * 查询用户当前可访问的租户。
     *
     * @param userId 用户ID
     * @return 可访问租户列表
     */
    List<SysTenant> getTenantListByUserId(Long userId);

    /**
     * 查询当前租户已有成员。
     *
     * @return 成员列表
     */
    List<MemberVO> listMembers();

    /** 按本次操作范围读取目标成员。 */
    SysUserTenant getRequiredMemberInDataScope(Long userId, DataScopeDecisionDTO scope);

    /** 在写事务中锁定并校验目标成员。 */
    SysUserTenant lockRequiredMemberInDataScope(Long userId, DataScopeDecisionDTO scope);

    /** 确认一组已关联成员都处于本次读取范围内。 */
    void requireMembersInDataScope(Set<Long> userIds, DataScopeDecisionDTO scope);

    /** 在写事务中锁定一组成员，并确认受限范围内没有越权目标。 */
    List<SysUserTenant> lockMembersInDataScope(Set<Long> userIds, DataScopeDecisionDTO scope);

    /** 查询有效成员在指定租户的有效主部门ID。 */
    Long getActiveDepartmentId(Long userId, Long tenantId);

    /** 修改当前租户成员的主部门；空值表示取消归属。 */
    void setMemberDepartment(Long userId, Long deptId);

    /**
     * 修改当前租户成员状态。
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void setMemberStatus(Long userId, Integer status);
}

