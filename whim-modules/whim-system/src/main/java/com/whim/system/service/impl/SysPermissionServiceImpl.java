package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.model.dto.permission.RolePermissionDTO;
import com.whim.system.model.dto.permission.SysMenuQueryDTO;
import com.whim.system.model.dto.permission.SysMenuSaveDTO;
import com.whim.system.model.dto.permission.SysMenuUpdateDTO;
import com.whim.system.model.dto.role.SysRolePermissionDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.enums.SysMenuType;
import com.whim.system.model.vo.permission.SysMenuTreeVO;
import com.whim.system.model.vo.permission.SysMenuVO;
import com.whim.system.model.vo.permission.SysRoleMenuTreeVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 菜单权限维护及登录授权查询。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final ISysRoleService roleService;
    private final ISysRolePermissionService rolePermissionService;
    private final ISysUserRoleService userRoleService;

    /**
     * 根据id查询菜单详情。
     *
     * @param id 菜单id
     * @return 菜单详情
     */
    @Override
    public SysMenuVO getMenuDetail(Long id) {
        SysPermission permission = this.getById(id);
        if (Objects.isNull(permission)) {
            throw new ServiceException("菜单不存在");
        }
        return BeanConvertUtils.convert(permission, SysMenuVO.class);
    }

    /**
     * 查询菜单列表，按名称筛选时保留祖先和匹配节点的子树。
     *
     * @param query 查询参数
     * @return 菜单树列表
     */
    @Override
    public List<SysMenuVO> getMenuList(SysMenuQueryDTO query) {
        List<SysPermission> permissions = this.list(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort, SysPermission::getId));
        List<SysMenuVO> menus = getMenuTree(permissions);
        return StringUtils.isBlank(query.getMenuName()) ? menus : filterMenuTree(menus, query.getMenuName());
    }

    /**
     * 获取动态路由，超级管理员拥有全部有效菜单，普通账号按登录菜单授权返回。
     *
     * @return 保留隐藏页面、排除按钮和停用分支的路由菜单树
     */
    @Override
    public List<SysMenuVO> getUserMenus() {
        UserInfo userInfo = authenticationContext.getCurrentUserInfo();
        List<SysPermission> permissions = this.list(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 0)
                .ne(SysPermission::getMenuType, SysMenuType.BUTTON.getCode())
                .orderByAsc(SysPermission::getSort, SysPermission::getId));
        Map<Long, SysPermission> permissionMap = permissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, permission -> permission));
        Set<Long> menuIds = new HashSet<>();
        for (SysPermission permission : permissions) {
            if (!userInfo.isSuperAdministrator() && !userInfo.getMenuIdSet().contains(permission.getId())) {
                continue;
            }
            Set<Long> branchIds = new HashSet<>();
            SysPermission current = permission;
            while (current != null && branchIds.add(current.getId())) {
                if (current.getParentId() == 0L) {
                    menuIds.addAll(branchIds);
                    break;
                }
                current = permissionMap.get(current.getParentId());
            }
        }
        return getMenuTree(permissions.stream().filter(permission -> menuIds.contains(permission.getId())).toList());
    }

    /**
     * 获取菜单下拉树列表。
     *
     * @return 包含按钮和停用节点的菜单树列表
     */
    @Override
    public List<SysMenuTreeVO> getMenuTreeList() {
        return getMenuSelectTree(this.list(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort, SysPermission::getId)));
    }

    /**
     * 获取角色菜单授权树。
     *
     * @param roleId 角色id
     * @return 完整菜单树和角色已授权的菜单id
     */
    @Override
    @Transactional(readOnly = true)
    public SysRoleMenuTreeVO getRoleMenuTree(Long roleId) {
        SysRole role = roleService.getById(roleId);
        if (Objects.isNull(role)) {
            throw new ServiceException("角色不存在");
        }
        List<SysPermission> permissions = this.list(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort, SysPermission::getId));
        Set<Long> grantedIds = getMenuIdsByRoleIds(Set.of(roleId));
        SysRoleMenuTreeVO tree = new SysRoleMenuTreeVO();
        tree.setMenus(getMenuSelectTree(permissions));
        tree.setCheckedKeys(permissions.stream().map(SysPermission::getId).filter(grantedIds::contains).toList());
        return tree;
    }

    /**
     * 登录时查询角色关联的菜单id，供路由查询使用。
     *
     * @param roleIds 角色id集合
     * @return 角色直接授予的菜单id集合
     */
    @Override
    public Set<Long> getMenuIdsByRoleIds(Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return rolePermissionService.list(new LambdaQueryWrapper<SysRolePermission>()
                        .select(SysRolePermission::getPermissionId).in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
    }

    /**
     * 根据权限码查询有效权限。
     *
     * @param permissionCode 精确权限码
     * @return 有效的页面或按钮权限，不存在时返回null
     */
    @Override
    public SysPermission getPermissionByCode(String permissionCode) {
        return lambdaQuery()
                .eq(SysPermission::getPerms, permissionCode)
                .eq(SysPermission::getStatus, 0)
                .in(SysPermission::getMenuType, SysMenuType.PAGE.getCode(), SysMenuType.BUTTON.getCode())
                .one();
    }

    /**
     * 构建权限码与授权角色的对应关系，供功能权限和数据权限共用。
     *
     * @param roles 登录时已核验的有效角色列表
     * @return 权限码与授权角色id集合的对应关系
     */
    @Override
    public Map<String, Set<Long>> getPermissionRoleIds(List<RoleInfo> roles) {
        if (roles.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds = new HashSet<>();
        for (RoleInfo role : roles) {
            roleIds.add(role.getRoleId());
        }
        Map<String, Set<Long>> permissionRoleIds = new LinkedHashMap<>();
        // 每项功能仅来自真实角色授权关系，不按角色名称生成额外权限。
        for (RolePermissionDTO permission : baseMapper.getRolePermissionsByRoleIds(roleIds)) {
            permissionRoleIds.computeIfAbsent(permission.getPermissionCode(), key -> new LinkedHashSet<>())
                    .add(permission.getRoleId());
        }
        return permissionRoleIds;
    }

    /**
     * 按菜单新增操作权限创建菜单定义，不自动授予任何角色。
     *
     * @param parameter 菜单新增参数
     * @return 新增菜单id
     */
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addMenu(SysMenuSaveDTO parameter) {
        SysPermission permission = BeanConvertUtils.convert(parameter, SysPermission.class);
        permission.setBuiltIn(0);
        validateMenu(permission);
        if (!this.save(permission)) {
            throw new ServiceException("新增菜单失败");
        }
        return permission.getId();
    }

    /**
     * 保存角色明确选择的菜单和按钮权限，不隐式授予祖先或后代。
     *
     * @param parameter 角色ID及完整菜单权限集合
     */
    @Override
    // 与菜单定义及角色删除共用组织锁，避免校验后节点被删除或关联用户同时变化。
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(SysRolePermissionDTO parameter) {
        SysRole role = roleService.getById(parameter.getId());
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        Set<Long> requested = new LinkedHashSet<>(parameter.getPermissionIds());
        if (!requested.isEmpty() && this.lambdaQuery().in(SysPermission::getId, requested).count() != requested.size()) {
            throw new ServiceException("部分菜单权限已被删除，请重新加载后选择");
        }
        Set<Long> current = getMenuIdsByRoleIds(Set.of(role.getId()));
        Set<Long> added = new LinkedHashSet<>(requested);
        added.removeAll(current);
        Set<Long> removed = new LinkedHashSet<>(current);
        removed.removeAll(requested);
        if (added.isEmpty() && removed.isEmpty()) {
            return;
        }
        // 独立勾选逐项保存，选择目录不隐式授予按钮，取消目录也不擅自删除已勾选子项。
        if (!removed.isEmpty()) {
            rolePermissionService.lambdaUpdate().eq(SysRolePermission::getRoleId, role.getId())
                    .in(SysRolePermission::getPermissionId, removed).remove();
        }
        if (!added.isEmpty()) {
            List<SysRolePermission> relations = added.stream().map(permissionId -> {
                SysRolePermission relation = new SysRolePermission();
                relation.setRoleId(role.getId());
                relation.setPermissionId(permissionId);
                return relation;
            }).toList();
            if (!rolePermissionService.saveBatch(relations)) {
                throw new ServiceException("保存角色权限失败");
            }
        }
        // 用户关联由组织锁保护，只有事务成功提交后才失效全部受影响的旧授权快照。
        List<Long> userIds = userRoleService.lambdaQuery().select(SysUserRole::getUserId)
                .eq(SysUserRole::getRoleId, role.getId()).list().stream().map(SysUserRole::getUserId).toList();
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
    }

    /**
     * 修改菜单权限，授权变化在事务提交后使相关会话失效。
     *
     * @param parameter 菜单ID及完整修改参数
     */
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMenu(SysMenuUpdateDTO parameter) {
        Long id = parameter.getId();
        SysPermission oldPermission = this.getById(id);
        if (Objects.isNull(oldPermission)) {
            throw new ServiceException("菜单不存在");
        }
        SysPermission permission = BeanConvertUtils.convert(parameter, SysPermission.class);
        permission.setBuiltIn(oldPermission.getBuiltIn());
        validateMenu(permission);
        boolean definitionChanged = !Objects.equals(oldPermission.getMenuType(), permission.getMenuType())
                || !Objects.equals(oldPermission.getPerms(), permission.getPerms());
        if (definitionChanged && oldPermission.getBuiltIn() == 1) {
            throw new ServiceException("内置菜单的类型和权限标识不能修改");
        }
        if (oldPermission.isProtectedNode() && permission.getStatus() != 0) {
            throw new ServiceException("核心菜单管理节点不能停用");
        }
        if (!Objects.equals(oldPermission.getMenuType(), permission.getMenuType())
                && this.exists(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id))) {
            throw new ServiceException("存在子节点的菜单不能修改类型");
        }
        if (permission.getStatus() == 1 && this.exists(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, id).eq(SysPermission::getStatus, 0))) {
            throw new ServiceException("请先停用所有启用的子节点");
        }
        if (!this.updateById(permission)) {
            throw new ServiceException("修改菜单失败");
        }
        if (definitionChanged || !Objects.equals(oldPermission.getStatus(), permission.getStatus())) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, baseMapper.getUserIdsByPermissionId(id));
        }
    }

    /**
     * 删除没有子节点的菜单并清理角色授权，内置首页和核心菜单管理节点不能删除。
     *
     * @param id 菜单id
     */
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMenu(Long id) {
        SysPermission permission = this.getById(id);
        if (Objects.isNull(permission)) {
            throw new ServiceException("菜单不存在");
        }
        if (permission.getBuiltIn() == 1 && "system:home:view".equals(permission.getPerms())) {
            throw new ServiceException("系统内置首页不能删除");
        }
        if (permission.isProtectedNode()) {
            throw new ServiceException("核心菜单管理节点不能删除");
        }
        if (this.exists(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id))) {
            throw new ServiceException("菜单存在子节点，请先删除子节点");
        }
        Set<Long> userIds = baseMapper.getUserIdsByPermissionId(id);
        rolePermissionService.remove(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id));
        if (!this.removeById(id)) {
            throw new ServiceException("删除菜单失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
    }

    /**
     * 将排序后的菜单列表组装为菜单树。
     *
     * @param permissions 按排序值和主键升序排列的菜单列表
     * @return 菜单树列表
     */
    private List<SysMenuVO> getMenuTree(List<SysPermission> permissions) {
        Map<Long, SysMenuVO> menuMap = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            menuMap.put(permission.getId(), BeanConvertUtils.convert(permission, SysMenuVO.class));
        }
        List<SysMenuVO> menus = new ArrayList<>();
        for (SysMenuVO menu : menuMap.values()) {
            if (menu.getParentId() == 0L) {
                menus.add(menu);
            } else if (menuMap.containsKey(menu.getParentId())) {
                menuMap.get(menu.getParentId()).getChildren().add(menu);
            }
        }
        return menus;
    }

    /**
     * 组装菜单选择树，保留按钮和停用节点。
     *
     * @param permissions 按排序值和主键升序排列的菜单列表
     * @return 菜单选择树列表
     */
    private List<SysMenuTreeVO> getMenuSelectTree(List<SysPermission> permissions) {
        Map<Long, SysMenuTreeVO> menuMap = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            SysMenuTreeVO menu = new SysMenuTreeVO();
            menu.setId(permission.getId());
            menu.setLabel(permission.getMenuName());
            menu.setMenuType(permission.getMenuType());
            menu.setStatus(permission.getStatus());
            menuMap.put(menu.getId(), menu);
        }
        List<SysMenuTreeVO> menus = new ArrayList<>();
        for (SysPermission permission : permissions) {
            SysMenuTreeVO menu = menuMap.get(permission.getId());
            if (permission.getParentId() == 0L) {
                menus.add(menu);
            } else if (menuMap.containsKey(permission.getParentId())) {
                menuMap.get(permission.getParentId()).getChildren().add(menu);
            }
        }
        return menus;
    }

    /**
     * 按名称筛选菜单树，保留祖先和匹配节点的完整子树。
     *
     * @param menus 待筛选的菜单树
     * @param menuName 菜单名称
     * @return 筛选后的菜单树
     */
    private List<SysMenuVO> filterMenuTree(List<SysMenuVO> menus, String menuName) {
        List<SysMenuVO> result = new ArrayList<>();
        for (SysMenuVO menu : menus) {
            if (menu.getMenuName().contains(menuName)) {
                result.add(menu);
                continue;
            }
            List<SysMenuVO> children = filterMenuTree(menu.getChildren(), menuName);
            if (!children.isEmpty()) {
                menu.setChildren(children);
                result.add(menu);
            }
        }
        return result;
    }

    /**
     * 清理菜单类型不适用的字段并校验唯一性和父子关系，调用前需持有菜单维护锁。
     *
     * @param permission 待保存的菜单权限，校验时清理当前类型不适用的字段
     */
    private void validateMenu(SysPermission permission) {
        boolean directory = permission.getMenuType().equals(SysMenuType.DIRECTORY.getCode());
        boolean page = permission.getMenuType().equals(SysMenuType.PAGE.getCode());
        boolean button = permission.getMenuType().equals(SysMenuType.BUTTON.getCode());
        permission.setPerms(page || button ? StringUtils.trimToNull(permission.getPerms()) : null);
        permission.setRouteName(directory || page ? StringUtils.trimToNull(permission.getRouteName()) : null);
        permission.setPath(button ? null : StringUtils.trimToNull(permission.getPath()));
        permission.setComponent(page ? StringUtils.trimToNull(permission.getComponent()) : null);
        if (!page) {
            permission.setParam(null);
            permission.setIsCache(0);
        }
        if (button) {
            permission.setIcon(null);
        }
        if (permission.getPerms() != null || permission.getRouteName() != null || permission.getPath() != null) {
            LambdaQueryWrapper<SysPermission> duplicate = new LambdaQueryWrapper<>();
            duplicate.ne(permission.getId() != null, SysPermission::getId, permission.getId());
            duplicate.and(wrapper -> wrapper
                    .eq(permission.getPerms() != null, SysPermission::getPerms, permission.getPerms())
                    .or(permission.getRouteName() != null)
                    .eq(permission.getRouteName() != null, SysPermission::getRouteName, permission.getRouteName())
                    .or(permission.getPath() != null)
                    .eq(permission.getPath() != null, SysPermission::getPath, permission.getPath()));
            if (this.exists(duplicate)) {
                throw new ServiceException("权限标识、路由名称或路由地址已存在");
            }
        }
        validateMenuParent(permission);
    }

    /**
     * 校验上级菜单类型、祖先启用状态和层级循环，调用前需持有菜单维护锁。
     *
     * @param permission 待保存的菜单权限
     */
    private void validateMenuParent(SysPermission permission) {
        boolean button = permission.getMenuType().equals(SysMenuType.BUTTON.getCode());
        Long parentId = permission.getParentId();
        if (parentId == 0L) {
            if (button) {
                throw new ServiceException("按钮必须挂在页面下");
            }
            return;
        }
        Map<Long, SysPermission> parents = this.list(new LambdaQueryWrapper<SysPermission>()
                        .select(SysPermission::getId, SysPermission::getParentId, SysPermission::getMenuType, SysPermission::getStatus))
                .stream().collect(Collectors.toMap(SysPermission::getId, menu -> menu));
        SysPermission parent = parents.get(parentId);
        Integer expectedParentType = button ? SysMenuType.PAGE.getCode() : SysMenuType.DIRECTORY.getCode();
        if (parent == null || !parent.getMenuType().equals(expectedParentType)) {
            throw new ServiceException("按钮的上级必须是页面，其他菜单的上级必须是目录");
        }
        Set<Long> ancestorIds = new HashSet<>();
        if (permission.getId() != null) {
            ancestorIds.add(permission.getId());
        }
        while (parentId != 0L) {
            if (!ancestorIds.add(parentId)) {
                throw new ServiceException("上级菜单不能是自身或其子孙节点");
            }
            parent = parents.get(parentId);
            if (parent == null) {
                throw new ServiceException("上级菜单层级不完整");
            }
            if (permission.getStatus() == 0 && parent.getStatus() != 0) {
                throw new ServiceException("请先启用上级菜单");
            }
            parentId = parent.getParentId();
        }
    }
}
