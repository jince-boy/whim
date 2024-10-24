package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.model.entity.SysPermission;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 菜单权限(SysPermission)表服务实现类
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

