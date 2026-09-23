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
     * @param tenantId 当前租户ID，平台上下文时为空
     * @param roleIds 当前用户已启用角色ID集合
     * @return 权限编码集合
     */
    Set<String> getPermissionCodeSetByUserIdAndTenantId(Long userId, Long tenantId, Set<Long> roleIds);

    /**
     * 修改权限，并强制当前拥有该权限的用户退出登录。
     *
     * @param entity 权限实体
     * @return 是否修改成功
     */
    @Override
    boolean updateById(SysPermission entity);
}

