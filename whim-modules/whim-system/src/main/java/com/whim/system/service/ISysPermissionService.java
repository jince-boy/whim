package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.permission.PermissionVO;

import java.util.List;
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

    /** 查询当前租户套餐可分配的权限。 */
    List<PermissionVO> listCurrentTenantAssignablePermissions();

    /** 查询平台权限目录。 */
    List<PermissionVO> listPlatformPermissions();

    /** 修改平台权限状态。 */
    void setPermissionStatus(Long permissionId, Integer status);

    /**
     * 修改权限，提交成功后使相关用户的旧授权失效。
     *
     * @param entity 权限实体
     * @return 是否修改成功
     */
    @Override
    boolean updateById(SysPermission entity);
}

