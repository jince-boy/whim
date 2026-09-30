package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRolePermissionDeptMapper;
import com.whim.system.model.entity.SysRolePermissionDept;
import com.whim.system.service.ISysRolePermissionDeptService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 操作级自定义部门范围持久化实现。
 */
@Service
public class SysRolePermissionDeptServiceImpl
        extends ServiceImpl<SysRolePermissionDeptMapper, SysRolePermissionDept> implements ISysRolePermissionDeptService {
}
