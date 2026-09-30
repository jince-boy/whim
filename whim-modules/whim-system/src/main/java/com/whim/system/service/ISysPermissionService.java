package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.permission.PermissionVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysPermission业务服务。
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /** 查询账号的有效操作权限码集合。 */
    Set<String> getPermissionCodeSetByUserId(Long userId);
    /** 查询系统权限目录。 */
    List<PermissionVO> listPermissions();
    /** 修改权限状态并在事务提交后撤销相关会话。 */
    void setPermissionStatus(Long permissionId, Integer status);
    /** 修改权限后撤销旧授权。 */
    @Override
    boolean updateById(SysPermission entity);
}

