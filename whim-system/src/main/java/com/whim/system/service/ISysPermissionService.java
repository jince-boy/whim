package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.model.entity.SysPermission;

import java.util.List;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 菜单权限(SysPermission)表服务接口
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * 通过用户id获取权限标识列表
     *
     * @param userId 用户Id
     * @return 权限标识列表
     */
    List<String> getPermissionCodeByUserId(Long userId);
}
