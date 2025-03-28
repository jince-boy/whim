package com.whim.system.provider;

import com.whim.core.auth.provider.IPermissionProvider;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jince
 * date: 2025/3/28 22:47
 * description: 权限提供者实现类，用于为系统用户提供权限和角色信息。
 * 该类实现了IPermissionProvider接口，通过依赖注入的ISysPermissionService和ISysRoleService服务，
 * 提供了基于用户ID的权限列表和角色列表查询功能。
 */
@Component("system")
@RequiredArgsConstructor
public class SysPermissionProvider implements IPermissionProvider {
    private final ISysPermissionService sysPermissionService;
    private final ISysRoleService sysRoleService;

    /**
     * 根据用户ID获取权限列表。
     *
     * @param id 用户ID
     * @return 权限代码列表
     */
    @Override
    public List<String> getPermissionList(Long id) {
        return sysPermissionService.getPermissionCodeByUserId(id);
    }

    /**
     * 根据用户ID获取角色列表。
     *
     * @param id 用户ID
     * @return 角色代码列表
     */
    @Override
    public List<String> getRoleList(Long id) {
        return sysRoleService.getRoleCodeByUserId(id);
    }
}
