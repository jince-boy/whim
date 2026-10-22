package com.whim.system.service.impl;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.permission.UserMenuVO;
import com.whim.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 从登录授权快照生成导航树，仅补齐已授权页面的有效祖先目录。
 */
@Service
@RequiredArgsConstructor
public class UserMenuService {
    private final ISysPermissionService permissionService;
    private final AuthenticationContext authenticationContext;

    /** 读取当前用户导航，不查询角色关系，不因目录存在而授予其下页面。 */
    public List<UserMenuVO> getUserMenus() {
        Set<String> granted = authenticationContext.getCurrentUserInfo().getPermissionCodeSet();
        List<SysPermission> permissions = permissionService.lambdaQuery()
                .eq(SysPermission::getStatus, 0).in(SysPermission::getMenuType, 1, 2)
                .orderByAsc(SysPermission::getSort).orderByAsc(SysPermission::getId).list();
        Map<Long, SysPermission> source = new LinkedHashMap<>();
        permissions.forEach(permission -> source.put(permission.getId(), permission));
        Set<Long> included = new HashSet<>();
        for (SysPermission page : permissions) {
            if (page.getMenuType() == 1 || page.getPerms() == null || !granted.contains(page.getPerms())) {
                continue;
            }
            Set<Long> branch = new HashSet<>();
            SysPermission node = page;
            boolean valid = false;
            for (int depth = 0; depth < 64 && node != null && branch.add(node.getId()); depth++) {
                if (Long.valueOf(0).equals(node.getParentId())) {
                    valid = true;
                    break;
                }
                node = source.get(node.getParentId());
                if (node != null && node.getMenuType() != 1) {
                    break;
                }
            }
            if (valid) included.addAll(branch);
        }
        Map<Long, UserMenuVO> nodes = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            if (included.contains(permission.getId())) {
                nodes.put(permission.getId(), BeanConvertUtils.convert(permission, UserMenuVO.class));
            }
        }
        List<UserMenuVO> roots = new ArrayList<>();
        for (UserMenuVO node : nodes.values()) {
            if (Long.valueOf(0).equals(node.getParentId())) {
                roots.add(node);
            } else {
                nodes.get(node.getParentId()).getChildren().add(node);
            }
        }
        return roots;
    }
}

