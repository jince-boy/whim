package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysTenantMapper;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.service.ISysTenantService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表服务实现类
 */
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {
}

