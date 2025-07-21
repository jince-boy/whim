package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.constant.MenuConstants;
import com.whim.core.constant.SystemConstants;
import com.whim.core.utils.ConvertUtils;
import com.whim.satoken.core.context.AuthContext;
import com.whim.satoken.service.PermissionProvider;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.MenuVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Jince
 * @date 2024-10-23 19:53:58
 * @description 权限提供者接口，用于为不同业务模块提供权限相关功能。
 * 该接口定义了两个核心方法：根据用户ID获取权限列表和角色列表。
 * 可以通过实现该接口来定制具体的权限管理逻辑。
 */
@Service("systemPermission")
@RequiredArgsConstructor
public class SysPermissionImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService, PermissionProvider {
    private final SysPermissionMapper sysPermissionMapper;
    private final ISysRoleService sysRoleService;

    /**
     * 通过用户id获取权限标识列表
     *
     * @param id 用户Id
     * @return 权限标识列表
     */
    @Override
    public Set<String> getPermissionList(Long id) {
        if (AuthContext.isSuperAdmin(id)) {
            return Set.of("*");
        } else {
            return sysPermissionMapper.getPermissionCodeByUserId(id);
        }
    }

    /**
     * 通过用户id获取角色权限标识列表
     *
     * @param id 用户id
     * @return 角色权限标识列表
     */
    @Override
    public Set<String> getRoleList(Long id) {
        return sysRoleService.getRoleCodeByUserId(id);
    }

    /**
     * 根据用户id获取菜单树
     *
     * @param id 用户id
     * @return 菜单树
     */
    @Override
    public List<MenuVO> getMenuTreeByUserId(Long id) {
        List<SysPermission> permissionList;
        if (AuthContext.isSuperAdmin(id)) {
            LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SysPermission::getType, MenuConstants.MENU_TYPE_DIR, MenuConstants.MENU_TYPE_MENU, MenuConstants.MENU_TYPE_LINK);
            wrapper.eq(SysPermission::getStatus, SystemConstants.STATUS_NORMAL);
            wrapper.orderByAsc(SysPermission::getParentId).orderByAsc(SysPermission::getSort);
            permissionList = this.list(wrapper);
        } else {
            permissionList = sysPermissionMapper.getMenuTreeByUserId(id);
        }
        if (CollectionUtils.isEmpty(permissionList)) {
            return Collections.emptyList();
        }
        // 使用Map优化查找性能
        Map<Long, List<SysPermission>> permissionMap = permissionList.stream()
                .collect(Collectors.groupingBy(SysPermission::getParentId));

        return buildMenuTree(permissionMap, MenuConstants.TOP_PARENT_ID);
    }

//    使用MyBatis-Plus的类型处理器（推荐）
//
//    这个方法不错，然后你帮我弄两个，一个map接收，一个list接收，然后

    /**
     * 递归构建菜单树
     */
    private List<MenuVO> buildMenuTree(Map<Long, List<SysPermission>> permissionMap, Long parentId) {
        List<SysPermission> permissions = permissionMap.getOrDefault(parentId, Collections.emptyList());
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }

        List<MenuVO> tree = new ArrayList<>();
        for (SysPermission permission : permissions) {
            MenuVO menuVO = ConvertUtils.convert(permission, MenuVO.class);
            // 递归查找子菜单
            List<MenuVO> children = buildMenuTree(permissionMap, permission.getId());
            menuVO.setChildren(children);
            tree.add(menuVO);
        }
        return tree;
    }
}

