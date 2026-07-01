package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysRole;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 系统角色表服务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 查询用户已启用角色编码集合。
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    Set<String> getRoleCodeSetByUserId(Long userId);
}
