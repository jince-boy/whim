package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.service.ISysDataScopeService;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jince
 * date: 2025/7/10 16:08
 * description: 系统数据权限服务实现类
 */
@Slf4j
@Service("systemDataScope")
@RequiredArgsConstructor
public class SysDataScopeServiceImpl implements ISysDataScopeService {
    private final ISysRoleDeptService sysRoleDeptService;
    private final ISysDeptService sysDeptService;

    /**
     * 获取角色自定义数据权限
     *
     * @param roleId 角色id
     * @return 自定义数据权限
     */
    @Override
    public String getRoleCustomDataPermission(Long roleId) {
        LambdaQueryWrapper<SysRoleDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysRoleDept::getDeptId);
        wrapper.eq(SysRoleDept::getRoleId, roleId);
        List<SysRoleDept> list = sysRoleDeptService.list(wrapper);
        return list.stream().map(SysRoleDept::getDeptId).map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 获取部门及子部门
     * @param deptId 部门id
     * @return 部门及子部门
     */
    @Override
    public String getDeptAndChildDept(Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDept::getId);
        wrapper.apply("FIND_IN_SET({0},ancestors) <> 0", deptId);
        List<SysDept> list = sysDeptService.list(wrapper);
        SysDept dept = new SysDept();
        dept.setId(deptId);
        list.add(dept);
        return list.stream().map(SysDept::getId).map(String::valueOf).collect(Collectors.joining(","));
    }
}
