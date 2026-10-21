package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.entity.SysRole;
import com.whim.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 查询登录账号的有效角色。
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    /** 查询当前有效角色。 */
    @Override
    public List<RoleInfo> getRoleInfoListByUserId(Long userId) {
        return baseMapper.selectRoleInfoListByUserId(userId);
    }
}
