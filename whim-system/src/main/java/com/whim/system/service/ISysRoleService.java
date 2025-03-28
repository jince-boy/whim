package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysRole;

import java.util.List;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 系统角色(SysRole)表服务接口
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * 通过用户id获取角色权限标识列表
     *
     * @param userId 用户id
     * @return 角色权限标识列表
     */
    List<String> getRoleCodeByUserId(Long userId);
}
