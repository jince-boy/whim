package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserRoleMapper;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.service.ISysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/06/30
 * @description 用户角色关联表服务实现类
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {
}

