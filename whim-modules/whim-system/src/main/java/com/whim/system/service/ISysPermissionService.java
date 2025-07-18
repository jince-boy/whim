package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;

import java.util.Set;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * @description 菜单权限(SysPermission)表服务接口
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * 根据用户id获取权限列表
     *
     * @param id 用户id
     * @return 权限列表
     */
    Set<String> getPermissionList(Long id);
}
