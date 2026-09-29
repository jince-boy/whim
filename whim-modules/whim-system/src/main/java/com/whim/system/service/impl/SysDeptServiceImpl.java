package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.dto.dept.DeptCreateDTO;
import com.whim.system.model.dto.dept.DeptUpdateDTO;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.model.vo.dept.DeptVO;
import com.whim.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统部门表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    private final SysDataScopeService dataScopeService;
    private final SysDeptPathService deptPathService;
    private final SysUserTenantMapper userTenantMapper;
    private final SysPostMapper postMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    /** 按本次列表动作的数据范围查询当前租户部门节点。 */
    @Override
    public List<DeptVO> listCurrentTenantDepartments() {
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:dept:list");
        var query = Wrappers.<SysDept>lambdaQuery().eq(SysDept::getTenantId, scope.getTenantId());
        if (!scope.isAll()) {
            if (scope.getDeptIds().isEmpty()) {
                throw new TenantAccessDeniedException("本次部门查询没有可用的数据范围");
            }
            query.in(SysDept::getId, scope.getDeptIds());
        }
        return list(query.orderByAsc(SysDept::getParentId, SysDept::getSort, SysDept::getId))
                .stream().map(department -> {
                    DeptVO response = new DeptVO();
                    response.setId(department.getId());
                    response.setParentId(department.getParentId());
                    response.setDeptName(department.getDeptName());
                    response.setSort(department.getSort());
                    response.setStatus(department.getStatus());
                    return response;
                }).toList();
    }

    /** 查询当前租户有效部门，不允许跨租户或停用部门参与授权。 */
    @Override
    public SysDept getRequiredActiveDepartment(Long deptId, Long tenantId) {
        return deptPathService.getRequiredActiveDepartment(deptId, tenantId);
    }

    /** 查询当前租户有效部门及其沿有效父子链可达的子部门。 */
    @Override
    public Set<Long> getActiveDescendantIds(Long deptId, Long tenantId) {
        return baseMapper.selectActiveDescendantIds(deptId, tenantId);
    }

    /** 创建当前租户部门并固定祖先路径。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCurrentTenantDepartment(DeptCreateDTO request) {
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:dept:create");
        Long tenantId = scope.getTenantId();
        Long parentId = request.getParentId();
        if (parentId < 0) {
            throw new IllegalArgumentException("父部门ID不能为负数");
        }
        String ancestors = "0";
        if (parentId == 0) {
            if (!scope.isAll()) {
                throw new TenantAccessDeniedException("只有全部范围可以创建根部门");
            }
        } else {
            SysDept parent = getById(parentId);
            ancestors = deptPathService.buildActiveDepartmentPath(parent, tenantId);
            if (!scope.isAll() && !scope.getDeptIds().contains(parentId)) {
                throw new TenantAccessDeniedException("父部门不在本次创建范围内");
            }
        }
        String name = request.getDeptName().trim();
        if (lambdaQuery().eq(SysDept::getTenantId, tenantId).eq(SysDept::getParentId, parentId)
                .eq(SysDept::getDeptName, name).exists()) {
            throw new IllegalArgumentException("同一父部门下已存在该部门名称");
        }
        SysDept department = new SysDept();
        department.setTenantId(tenantId);
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(name);
        department.setSort(request.getSort());
        department.setStatus(0);
        save(department);
        return department.getId();
    }

    /** 修改当前租户部门名称和排序，不隐式变更部门树路径。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentTenantDepartment(Long deptId, DeptUpdateDTO request) {
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:dept:update");
        Long tenantId = scope.getTenantId();
        if (!scope.isAll() && !scope.getDeptIds().contains(deptId)) {
            throw new TenantAccessDeniedException("目标部门不在本次修改范围内");
        }
        SysDept department = getById(deptId);
        if (department == null || !Objects.equals(department.getTenantId(), tenantId)) {
            throw new TenantAccessDeniedException("目标部门不属于当前租户");
        }
        String name = request.getDeptName().trim();
        if (lambdaQuery().eq(SysDept::getTenantId, tenantId)
                .eq(SysDept::getParentId, department.getParentId()).eq(SysDept::getDeptName, name)
                .ne(SysDept::getId, deptId).exists()) {
            throw new IllegalArgumentException("同一父部门下已存在该部门名称");
        }
        if (!lambdaUpdate().eq(SysDept::getId, deptId).eq(SysDept::getTenantId, tenantId)
                .set(SysDept::getDeptName, name).set(SysDept::getSort, request.getSort()).update()) {
            throw new TenantAccessDeniedException("目标部门已不可修改");
        }
    }

    /** 停用前确认部门无有效子部门及归属，启用前确认父部门有效。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentTenantDepartmentStatus(Long deptId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        DataScopeDecisionDTO scope = dataScopeService.resolveCurrentTenantDataScope("system:dept:status");
        Long tenantId = scope.getTenantId();
        if (!scope.isAll() && !scope.getDeptIds().contains(deptId)) {
            throw new TenantAccessDeniedException("目标部门不在本次状态操作范围内");
        }
        SysDept department = getById(deptId);
        if (department == null || !Objects.equals(department.getTenantId(), tenantId)) {
            throw new TenantAccessDeniedException("目标部门不属于当前租户");
        }
        if (status == 0 && department.getParentId() != 0) {
            getRequiredActiveDepartment(department.getParentId(), tenantId);
        }
        if (status == 1) {
            boolean hasActiveChild = lambdaQuery().eq(SysDept::getTenantId, tenantId)
                    .eq(SysDept::getParentId, deptId).eq(SysDept::getStatus, 0).exists();
            boolean hasMembers = userTenantMapper.exists(Wrappers
                    .<SysUserTenant>lambdaQuery().eq(SysUserTenant::getTenantId, tenantId)
                    .eq(SysUserTenant::getDeptId, deptId));
            boolean hasPosts = postMapper.exists(Wrappers
                    .<SysPost>lambdaQuery().eq(SysPost::getTenantId, tenantId)
                    .eq(SysPost::getDeptId, deptId));
            boolean hasRoleScopes = roleDeptMapper.exists(Wrappers
                    .<SysRoleDept>lambdaQuery().eq(SysRoleDept::getTenantId, tenantId)
                    .eq(SysRoleDept::getDeptId, deptId));
            if (hasActiveChild || hasMembers || hasPosts || hasRoleScopes) {
                throw new IllegalArgumentException("部门仍被子部门、成员、岗位或角色范围使用");
            }
        }
        if (!lambdaUpdate().eq(SysDept::getId, deptId).eq(SysDept::getTenantId, tenantId)
                .set(SysDept::getStatus, status).update()) {
            throw new TenantAccessDeniedException("目标部门已不可操作");
        }
    }
}

