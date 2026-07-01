package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 权限表(菜单和按钮权限)服务接口
 */
public interface ISysPermissionService extends IService<SysPermission> {

    /**
     * 查询用户已启用权限编码集合。
     *
     * @param userId 用户ID
     * @return 权限编码集合
     */
    Set<String> getPermissionCodeSetByUserId(Long userId);
}
