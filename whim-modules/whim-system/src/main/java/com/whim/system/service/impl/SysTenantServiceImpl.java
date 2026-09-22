package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysTenantMapper;
import com.whim.system.model.entity.SysTenant;
import com.whim.system.service.ISysTenantService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户表服务实现类
 */
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    /**
     * 查询全部当前可用租户。
     *
     * @return 可用租户列表
     */
    @Override
    public List<SysTenant> getAvailableTenantList() {
        return Objects.requireNonNullElse(baseMapper.selectAvailableTenantList(), List.of());
    }

    /**
     * 查询全部当前可用租户ID。
     *
     * @return 可用租户ID集合
     */
    @Override
    public Set<Long> getAvailableTenantIds() {
        return Objects.requireNonNullElse(baseMapper.selectAvailableTenantIds(), Set.of());
    }
}

