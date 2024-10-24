package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.model.entity.SysRole;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 系统角色(SysRole)表服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final SysRoleMapper sysRoleMapper;

    /**
     * 通过用户id获取角色权限标识列表
     *
     * @param userId 用户id
     * @return 角色权限标识列表
     */
    @Override
    public List<String> getRoleCodeByUserId(Long userId) {
        return sysRoleMapper.getRoleCodeByUserId(userId);
    }
}

