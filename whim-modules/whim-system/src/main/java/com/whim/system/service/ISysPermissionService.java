package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;

import java.util.Set;

/**
 * @author Jince
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

    /**
     * 修改权限，并强制当前拥有该权限的用户退出登录。
     *
     * @param entity 权限实体
     * @return 是否修改成功
     */
    @Override
    boolean updateById(SysPermission entity);
}

