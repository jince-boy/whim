package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRolePermissionMapper;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.service.ISysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色权限关联的通用持久化操作。
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission>
        implements ISysRolePermissionService {
}
