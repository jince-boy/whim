package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysUserTenantMapper;
import com.whim.system.model.entity.SysUserTenant;
import com.whim.system.service.ISysUserTenantService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户租户关联表服务实现类
 */
@Service
public class SysUserTenantServiceImpl extends ServiceImpl<SysUserTenantMapper, SysUserTenant> implements ISysUserTenantService {
}

