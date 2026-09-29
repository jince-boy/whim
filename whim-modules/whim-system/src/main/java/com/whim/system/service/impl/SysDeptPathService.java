package com.whim.system.service.impl;

import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.entity.SysDept;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 校验当前租户的有效部门祖先链。
 */
@Service
@RequiredArgsConstructor
public class SysDeptPathService {
    private final SysDeptMapper deptMapper;

    /** 查询当前租户的有效部门并确认整条祖先链可用。 */
    public SysDept getRequiredActiveDepartment(Long deptId, Long tenantId) {
        SysDept department = deptMapper.selectById(deptId);
        buildActiveDepartmentPath(department, tenantId);
        return department;
    }

    /** 沿真实父子关系构建完整祖先路径，拒绝跨租户、停用和循环引用。 */
    public String buildActiveDepartmentPath(SysDept department, Long tenantId) {
        List<Long> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        SysDept current = department;
        while (current != null) {
            if (!Objects.equals(current.getTenantId(), tenantId) || current.getStatus() != 0
                    || !visited.add(current.getId())) {
                throw new TenantAccessDeniedException("部门层级不属于当前租户或不可用");
            }
            path.add(current.getId());
            if (current.getParentId() == 0) {
                break;
            }
            current = deptMapper.selectById(current.getParentId());
        }
        if (current == null) {
            throw new TenantAccessDeniedException("部门上级不存在");
        }
        StringBuilder ancestors = new StringBuilder("0");
        for (int index = path.size() - 1; index >= 0; index--) {
            ancestors.append(',').append(path.get(index));
        }
        if (ancestors.length() > 500) {
            throw new TenantAccessDeniedException("部门层级过深或路径不可用");
        }
        return ancestors.toString();
    }
}
