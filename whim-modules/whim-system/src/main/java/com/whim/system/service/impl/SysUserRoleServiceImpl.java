package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.service.ISysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户角色关联的通用持久化操作。
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements ISysUserRoleService {
}
