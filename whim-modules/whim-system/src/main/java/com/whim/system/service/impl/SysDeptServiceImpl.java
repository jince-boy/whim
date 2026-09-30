package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.dto.dept.DeptCreateDTO;
import com.whim.system.model.dto.dept.DeptUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.vo.dept.DeptVO;
import com.whim.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 单组织部门管理及组织引用完整性校验。
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    private final SysDeptPathService deptPathService;

    /** 按本次操作范围返回部门节点，不补出未授权的其他节点。 */
    @Override
    @DataPermission(permission = "system:dept:list",
            tables = @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = ""))
    public List<DeptVO> listDepartments() {
        return lambdaQuery().orderByAsc(SysDept::getParentId, SysDept::getSort, SysDept::getId).list()
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

    /** 内部核验部门及其祖先链，业务访问另由操作范围控制。 */
    @Override
    public SysDept getRequiredActiveDepartment(Long deptId) {
        SysDept department = lambdaQuery().eq(SysDept::getId, deptId).last("FOR UPDATE").one();
        deptPathService.buildActiveDepartmentPath(department);
        return department;
    }

    /** 返回有效部门及其有效下级节点，供数据权限计算使用。 */
    @Override
    public Set<Long> getActiveDescendantIds(Long deptId) {
        getRequiredActiveDepartment(deptId);
        return baseMapper.selectActiveDescendantIds(deptId);
    }

    /** 创建根部门需全部范围，创建子部门需父部门处于创建范围。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:create",
            tables = @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = ""))
    public Long createDepartment(DeptCreateDTO request) {
        Long parentId = request.getParentId();
        if (parentId < 0) {
            throw new IllegalArgumentException("父部门ID不能为负数");
        }
        String ancestors = "0";
        if (parentId == 0) {
            DataPermissionContext.checkOwnership("sys_dept", null, 0L);
            if (!DataPermissionContext.requiredScope().isAll()) {
                throw new DataAccessDeniedException("只有全部范围可以创建根部门");
            }
        } else {
            DataPermissionContext.checkOwnership("sys_dept", null, parentId);
            SysDept parent = lambdaQuery().eq(SysDept::getId, parentId).last("FOR UPDATE").one();
            ancestors = deptPathService.buildActiveDepartmentPath(parent);
        }
        String name = request.getDeptName().trim();
        if (lambdaQuery().eq(SysDept::getParentId, parentId).eq(SysDept::getDeptName, name).exists()) {
            throw new IllegalArgumentException("同一父部门下已存在该部门名称");
        }
        SysDept department = new SysDept();
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(name);
        department.setSort(request.getSort());
        department.setStatus(0);
        save(department);
        return department.getId();
    }

    /** 修改部门名称和排序，不隐式变更组织归属或历史路径。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:update",
            tables = @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = ""))
    public void updateDepartment(Long deptId, DeptUpdateDTO request) {
        SysDept department = requiredDepartment(deptId);
        String name = request.getDeptName().trim();
        if (lambdaQuery().eq(SysDept::getParentId, department.getParentId())
                .eq(SysDept::getDeptName, name).ne(SysDept::getId, deptId).exists()) {
            throw new IllegalArgumentException("同一父部门下已存在该部门名称");
        }
        department.setDeptName(name);
        department.setSort(request.getSort());
        if (!updateById(department)) {
            throw new DataAccessDeniedException("部门已不可修改");
        }
    }

    /** 启用前确认父节点有效，停用前确认没有任何组织和授权引用。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:status",
            tables = @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = ""))
    public void setDepartmentStatus(Long deptId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysDept department = requiredDepartment(deptId);
        if (Objects.equals(department.getStatus(), status)) {
            return;
        }
        if (status == 0 && department.getParentId() != 0) {
            getRequiredActiveDepartment(department.getParentId());
        }
        if (status == 1 && baseMapper.hasDepartmentReferences(deptId)) {
            throw new IllegalArgumentException("部门仍被子部门、用户、岗位或角色数据范围使用");
        }
        department.setStatus(status);
        if (!updateById(department)) {
            throw new DataAccessDeniedException("部门已不可操作");
        }
    }

    /** 仅允许删除没有任何引用的部门。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:delete",
            tables = @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = ""))
    public void deleteDepartment(Long deptId) {
        SysDept department = requiredDepartment(deptId);
        if (baseMapper.hasDepartmentReferences(deptId)) {
            throw new IllegalArgumentException("部门仍被引用，不能删除");
        }
        department.setDeleted(1);
        if (!removeById(department)) {
            throw new DataAccessDeniedException("部门已不可删除");
        }
    }

    /** 锁定本次操作可见的节点。 */
    private SysDept requiredDepartment(Long deptId) {
        SysDept department = lambdaQuery().eq(SysDept::getId, deptId).last("FOR UPDATE").one();
        if (department == null) {
            throw new DataAccessDeniedException("部门不存在或不在本次操作范围内");
        }
        return department;
    }
}

