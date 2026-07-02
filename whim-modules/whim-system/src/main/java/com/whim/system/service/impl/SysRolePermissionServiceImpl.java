package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRolePermissionMapper;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.service.ISysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色权限关联表服务实现类
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {
}

