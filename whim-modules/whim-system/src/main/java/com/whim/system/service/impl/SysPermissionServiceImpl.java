package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.system.mapper.SysPermissionMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.dto.menu.MenuSaveDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysRolePermissionDept;
import com.whim.system.model.vo.menu.MenuVO;
import com.whim.system.model.vo.permission.PermissionVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRolePermissionDeptService;
import com.whim.system.service.ISysRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 统一维护菜单、按钮权限、用户导航及角色授权失效。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
    private final AuthenticationContext authenticationContext;
    private final AuthenticationSession authenticationSession;
    private final SysAuthorizationService authorizationService;
    private final ISysRolePermissionService rolePermissionService;
    private final ISysRolePermissionDeptService permissionDeptService;
    private final SysRoleMapper roleMapper;

    /** 详情不复用列表权限，必须具备精确详情操作权限。 */
    @Override
    public MenuVO getMenu(Long menuId) {
        return toMenuVO(requiredMenu(menuId));
    }

    /** 查询账号通过有效角色获得的精确权限码。 */
    @Override
    public Set<String> getPermissionCodeSetByUserId(Long userId) {
        if (Boolean.TRUE.equals(roleMapper.selectSuperAdministratorFlag(userId))) {
            return lambdaQuery().eq(SysPermission::getStatus, 0).in(SysPermission::getMenuType, 2, 3)
                    .isNotNull(SysPermission::getPerms).ne(SysPermission::getPerms, "").list().stream()
                    .map(SysPermission::getPerms).collect(Collectors.toSet());
        }
        return baseMapper.selectPermissionCodeSetByUserId(userId);
    }

    /** 查询角色授权使用的权限选项，不隐含目录下的操作授权。 */
    @Override
    public List<PermissionVO> listPermissions() {
        return lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId).list().stream().map(permission -> {
            PermissionVO response = new PermissionVO();
            response.setId(permission.getId());
            response.setMenuName(permission.getMenuName());
            response.setPerms(permission.getPerms());
            response.setStatus(permission.getStatus());
            response.setDataPermission(permission.getDataPermission());
            response.setParentId(permission.getParentId());
            response.setMenuType(permission.getMenuType());
            return response;
        }).toList();
    }

    /** 管理搜索保留祖先，父节点选择应使用不带搜索条件的完整管理树。 */
    @Override
    public List<MenuVO> listMenus(String menuName) {
        List<SysPermission> catalog = lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId).list();
        if (!StringUtils.hasText(menuName)) return buildTree(catalog);
        Map<Long, SysPermission> index = catalog.stream().collect(Collectors.toMap(SysPermission::getId, item -> item));
        Set<Long> included = new HashSet<>();
        for (SysPermission item : catalog) {
            if (!item.getMenuName().contains(menuName.trim())) continue;
            SysPermission current = item;
            while (current != null && included.add(current.getId())) current = index.get(current.getParentId());
        }
        return buildTree(catalog.stream().filter(item -> included.contains(item.getId())).toList());
    }

    /** 只返回被直接授予的有效页面和外链；祖先只补导航，不隐式授予任何操作。 */
    @Override
    public List<MenuVO> getCurrentMenus() {
        boolean administrator = authenticationContext.isSuperAdministrator();
        Set<Long> grants = administrator ? Set.of() : baseMapper.selectGrantedMenuIds(authenticationContext.getUserId());
        List<SysPermission> catalog = lambdaQuery().eq(SysPermission::getStatus, 0)
                .ne(SysPermission::getMenuType, 3).orderByAsc(SysPermission::getSort, SysPermission::getId).list();
        Map<Long, SysPermission> index = catalog.stream().collect(Collectors.toMap(SysPermission::getId, item -> item));
        Set<Long> included = new HashSet<>();
        for (SysPermission item : catalog) {
            if (item.getMenuType() == 1 || (!administrator && !grants.contains(item.getId()))) continue;
            Set<Long> ancestry = new HashSet<>();
            SysPermission current = item;
            boolean valid = true;
            while (current != null) {
                if (!ancestry.add(current.getId())) { valid = false; break; }
                if (current.getParentId() == 0) break;
                current = index.get(current.getParentId());
                if (current == null || current.getMenuType() != 1) { valid = false; break; }
            }
            if (valid) included.addAll(ancestry);
        }
        return buildTree(catalog.stream().filter(item -> included.contains(item.getId())).toList());
    }

    /** 新的权限定义由超级管理员登记，普通管理者不能创建自己未持有的操作。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuSaveDTO request) {
        lockCatalog();
        SysPermission menu = validateMenu(null, request);
        menu.setBuiltIn(0);
        save(menu);
        return menu.getId();
    }

    /** 展示变更不强制退出；授权语义或页面操作状态改变时撤销受影响会话。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Long menuId, MenuSaveDTO request) {
        lockCatalog();
        SysPermission existing = requiredMenu(menuId);
        SysPermission menu = validateMenu(existing, request);
        boolean semanticsChanged = !Objects.equals(existing.getPerms(), menu.getPerms())
                || !Objects.equals(existing.getDataPermission(), menu.getDataPermission())
                || !Objects.equals(existing.getMenuType(), menu.getMenuType());
        if (existing.getBuiltIn() == 1 && semanticsChanged) throw new IllegalArgumentException("内置节点的类型、权限标识和数据权限规则不能修改");
        if (!authenticationContext.isSuperAdministrator()) {
            if (semanticsChanged || !Objects.equals(existing.getStatus(), menu.getStatus()))
                throw new DataAccessDeniedException("只有超级管理员可以修改权限语义或操作状态");
            authorizationService.requireGrantablePermission(menuId);
        }
        if (isProtectedMenu(existing)) {
            if (menu.getStatus() != 0 || menu.getVisible() != 0 || !Objects.equals(existing.getParentId(), menu.getParentId()))
                throw new IllegalArgumentException("内置菜单管理入口及其操作不能停用、隐藏或迁移");
        }
        Set<Long> affected = semanticsChanged || existing.getMenuType() != 1 && !Objects.equals(existing.getStatus(), menu.getStatus())
                ? baseMapper.selectUserIdSetByPermissionId(menuId) : Set.of();
        menu.setId(menuId);
        // 显式写入可空字段，类型切换或清空输入不能被 MyBatis-Plus 的非空更新策略忽略。
        update(menu, lambdaUpdate().eq(SysPermission::getId, menuId)
                .set(menu.getRouteName() == null, SysPermission::getRouteName, null)
                .set(menu.getPath() == null, SysPermission::getPath, null)
                .set(menu.getComponent() == null, SysPermission::getComponent, null)
                .set(menu.getParam() == null, SysPermission::getParam, null)
                .set(menu.getPerms() == null, SysPermission::getPerms, null)
                .set(menu.getIcon() == null, SysPermission::getIcon, null)
                .set(menu.getRemark() == null, SysPermission::getRemark, null).getWrapper());
        if (!affected.isEmpty()) authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affected);
    }

    /** 校验状态管理边界，保留核心管理入口。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPermissionStatus(Long permissionId, Integer status) {
        lockCatalog();
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0或1");
        }
        SysPermission permission = requiredMenu(permissionId);
        if (!authenticationContext.isSuperAdministrator()) {
            authorizationService.requireGrantablePermission(permissionId);
        }
        if (status == 1 && isProtectedMenu(permission)) {
            throw new IllegalArgumentException("不能停用核心菜单管理入口及其操作");
        }
        Set<Long> affected = baseMapper.selectUserIdSetByPermissionId(permissionId);
        permission.setStatus(status);
        updateById(permission);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affected);
    }

    /** 删除叶子节点并清理角色授权及操作数据范围，提交后撤销旧授权。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        lockCatalog();
        SysPermission menu = requiredMenu(menuId);
        if (isProtectedMenu(menu)) throw new IllegalArgumentException("核心菜单管理入口及其操作不能删除");
        if (lambdaQuery().eq(SysPermission::getParentId, menuId).exists()) throw new IllegalArgumentException("请先删除所有子节点");
        Set<Long> affected = baseMapper.selectUserIdSetByPermissionId(menuId);
        permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getPermissionId, menuId).remove();
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getPermissionId, menuId).remove();
        menu.setDeleted(1);
        removeById(menu);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affected);
    }

    /** 核心管理节点必须保留，其他内置节点仍可执行删除。 */
    private boolean isProtectedMenu(SysPermission menu) {
        return menu.getBuiltIn() == 1 && (menu.getMenuType() == 1 && menu.getParentId() == 0
                || menu.getPerms() != null && (menu.getPerms().startsWith("system:menu:")
                || menu.getPerms().startsWith("system:permission:")));
    }

    /** 返回统一的节点保护标记，前端不重复维护内置权限清单。 */
    private MenuVO toMenuVO(SysPermission menu) {
        MenuVO response = BeanConvertUtils.convert(menu, MenuVO.class);
        response.setProtectedNode(isProtectedMenu(menu));
        return response;
    }

    /** 查询有效定义，不把不存在的节点解释成顶级菜单。 */
    private SysPermission requiredMenu(Long menuId) {
        SysPermission menu = getById(menuId);
        if (menu == null) throw new IllegalArgumentException("菜单不存在或已删除");
        return menu;
    }

    /** 树结构写操作持有同一个事务锁，防止并发校验分别通过后形成循环。 */
    private void lockCatalog() {
        if (baseMapper.lockMenuCatalog() == null) throw new IllegalStateException("菜单维护锁不存在，请检查数据库迁移");
    }

    /** 归一化字段并校验父子类型、祖先链、唯一标识与安全路径。 */
    private SysPermission validateMenu(SysPermission existing, MenuSaveDTO request) {
        SysPermission menu = BeanConvertUtils.convert(request, SysPermission.class);
        menu.setMenuName(menu.getMenuName().trim());
        menu.setPerms(StringUtils.hasText(menu.getPerms()) ? menu.getPerms().trim() : null);
        menu.setRouteName(StringUtils.hasText(menu.getRouteName()) ? menu.getRouteName().trim() : null);
        menu.setPath(StringUtils.hasText(menu.getPath()) ? menu.getPath().trim() : null);
        int type = menu.getMenuType();
        if (type == 2 || type == 3) {
            if (menu.getPerms() == null || !menu.getPerms().matches("[a-z][A-Za-z0-9]*(?::[a-z][A-Za-z0-9]*){2,}"))
                throw new IllegalArgumentException("页面和按钮必须填写精确权限标识，例如system:menu:list");
        } else {
            menu.setPerms(null);
            menu.setDataPermission(0);
        }
        if (type == 1 || type == 2) {
            if (menu.getRouteName() == null || !menu.getRouteName().matches("[A-Za-z][A-Za-z0-9]*")
                    || Set.of("Layout", "UserProfile", "NoAccess", "NotFound", "login").contains(menu.getRouteName()))
                throw new IllegalArgumentException("路由名称须由字母和数字组成且不能使用系统保留名称");
            if (menu.getPath() == null || !menu.getPath().matches("/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*)(?:/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*))*")
                    || menu.getPath().matches("/(?:login|profile|noAccess|redirect)(?:/.*)?"))
                throw new IllegalArgumentException("路由须使用绝对路径及小驼峰片段，不能占用系统保留路径");
            if (menu.getPath().contains("/:") && (type == 1 || menu.getVisible() == 0))
                throw new IllegalArgumentException("带动态参数的路由只能用于隐藏页面，不能作为目录或可点击菜单");
        } else {
            menu.setRouteName(null);
        }
        if (type == 2) {
            if (!StringUtils.hasText(menu.getComponent()) || !menu.getComponent().matches("[A-Za-z][A-Za-z0-9]*(?:/[A-Za-z][A-Za-z0-9]*)*"))
                throw new IllegalArgumentException("组件路径须相对于views目录，不含扩展名或路径跳转");
            if (StringUtils.hasText(menu.getParam()) && (!menu.getParam().matches("(?:[A-Za-z][A-Za-z0-9]*=[^&#=]*)(?:&[A-Za-z][A-Za-z0-9]*=[^&#=]*)*")
                    || menu.getParam().contains("\\"))) throw new IllegalArgumentException("路由参数须为key=value格式");
        } else {
            menu.setComponent(null);
            menu.setParam(null);
            menu.setIsCache(0);
        }
        if (type == 3) { menu.setPath(null); menu.setIcon(null); menu.setVisible(0); }
        if (type == 4) {
            try {
                URI uri = URI.create(menu.getPath());
                if (!Set.of("https", "http").contains(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null)
                    throw new IllegalArgumentException("外链必须是有效的HTTP或HTTPS地址");
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new IllegalArgumentException("外链必须是有效的HTTP或HTTPS地址");
            }
        }
        List<SysPermission> catalog = lambdaQuery().list();
        Map<Long, SysPermission> index = catalog.stream().collect(Collectors.toMap(SysPermission::getId, item -> item));
        SysPermission parent = index.get(menu.getParentId());
        if (menu.getParentId() != 0 && parent == null) throw new IllegalArgumentException("上级菜单不存在");
        if (type == 3 && (parent == null || parent.getMenuType() != 2)) throw new IllegalArgumentException("按钮权限必须属于页面菜单");
        if (type != 3 && parent != null && parent.getMenuType() != 1) throw new IllegalArgumentException("目录、页面和外链只能放在目录下");
        Set<Long> ancestry = new HashSet<>();
        while (parent != null) {
            if (!ancestry.add(parent.getId()) || existing != null && Objects.equals(parent.getId(), existing.getId()))
                throw new IllegalArgumentException("菜单不能移动到自身或后代节点下");
            if (menu.getStatus() == 0 && parent.getStatus() != 0) throw new IllegalArgumentException("启用菜单不能放在停用目录下");
            if (parent.getParentId() == 0) break;
            parent = index.get(parent.getParentId());
            if (parent == null) throw new IllegalArgumentException("上级菜单的祖先链不完整");
        }
        for (SysPermission item : catalog) {
            if (existing != null && Objects.equals(item.getId(), existing.getId())) continue;
            if (menu.getPerms() != null && menu.getPerms().equals(item.getPerms())) throw new IllegalArgumentException("权限标识已存在");
            if (menu.getRouteName() != null && menu.getRouteName().equalsIgnoreCase(item.getRouteName())) throw new IllegalArgumentException("路由名称已存在");
            if ((type == 1 || type == 2) && (item.getMenuType() == 1 || item.getMenuType() == 2)
                    && menu.getPath().equalsIgnoreCase(item.getPath())) throw new IllegalArgumentException("路由地址已存在");
            if (existing != null && Objects.equals(item.getParentId(), existing.getId())
                    && !(type == 1 && item.getMenuType() != 3 || type == 2 && item.getMenuType() == 3))
                throw new IllegalArgumentException("当前节点仍有不符合新类型的子节点");
        }
        return menu;
    }

    /** 按既定排序在线性时间组装树，查询结果中的节点不会重复展开。 */
    private List<MenuVO> buildTree(List<SysPermission> catalog) {
        Map<Long, MenuVO> nodes = new LinkedHashMap<>();
        for (SysPermission item : catalog) nodes.put(item.getId(), toMenuVO(item));
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : nodes.values()) {
            MenuVO parent = nodes.get(node.getParentId());
            if (parent == null) roots.add(node);
            else parent.getChildren().add(node);
        }
        return roots;
    }
}
