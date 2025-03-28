package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 权限提供者接口，用于为不同业务模块提供权限相关功能。
 * 该接口定义了两个核心方法：根据用户ID获取权限列表和角色列表。
 * 可以通过实现该接口来定制具体的权限管理逻辑。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 通过用户id获取权限标识列表
     *
     * @param userId 用户Id
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermissionCodeByUserId(Long userId) {
        return sysPermissionMapper.getPermissionCodeByUserId(userId);
    }
}

