package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRolePermissionMapper;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.service.ISysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 角色菜单权限关联的持久化实现。
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission>
        implements ISysRolePermissionService {
}
