package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统权限菜单表服务接口
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

