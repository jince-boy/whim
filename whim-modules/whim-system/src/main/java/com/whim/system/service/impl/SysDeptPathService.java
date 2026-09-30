package com.whim.system.service.impl;

import com.whim.core.exception.DataAccessDeniedException;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.entity.SysDept;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 只读校验有效部门祖先链，拒绝停用、断链和循环结构。
 */
@Service
@RequiredArgsConstructor
public class SysDeptPathService {
    private final SysDeptMapper deptMapper;

    /** 返回有效部门，并确认其整条祖先链有效。 */
    public SysDept getRequiredActiveDepartment(Long deptId) {
        SysDept department = deptMapper.selectAuthorizationDepartment(deptId);
        buildActiveDepartmentPath(department);
        return department;
    }

    /** 根据真实父子关系构建含当前部门的路径，用于校验和新建子部门。 */
    public String buildActiveDepartmentPath(SysDept department) {
        List<Long> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        SysDept current = department;
        while (current != null) {
            if (current.getStatus() != 0 || !visited.add(current.getId())) {
                throw new DataAccessDeniedException("部门层级停用或存在循环引用");
            }
            path.add(current.getId());
            if (current.getParentId() == 0) {
                break;
            }
            current = deptMapper.selectAuthorizationDepartment(current.getParentId());
        }
        if (current == null) {
            throw new DataAccessDeniedException("目标部门或上级部门不存在或已停用");
        }
        StringBuilder ancestors = new StringBuilder("0");
        for (int index = path.size() - 1; index >= 0; index--) {
            ancestors.append(',').append(path.get(index));
        }
        if (ancestors.length() > 500) {
            throw new DataAccessDeniedException("部门层级超过支持的路径长度");
        }
        return ancestors.toString();
    }
}
