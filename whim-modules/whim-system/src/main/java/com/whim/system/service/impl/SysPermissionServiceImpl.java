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
import com.whim.system.model.enums.SysMenuType;
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
            return lambdaQuery().eq(SysPermission::getStatus, 0)
                    .in(SysPermission::getMenuType, SysMenuType.PAGE.getCode(), SysMenuType.BUTTON.getCode())
                    .isNotNull(SysPermission::getPerms).ne(SysPermission::getPerms, "").list().stream()
                    .map(SysPermission::getPerms).collect(Collectors.toSet());
        }
        return baseMapper.selectPermissionCodeSetByUserId(userId);
    }

    /** 查询角色授权使用的权限选项，不隐含目录下的操作授权。 */
    @Override
    public List<PermissionVO> listPermissions() {
        List<SysPermission> menus = lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId).list();
        return BeanConvertUtils.convertList(menus, PermissionVO.class);
    }

    /** 管理搜索保留祖先，父节点选择应使用不带搜索条件的完整管理树。 */
    @Override
    public List<MenuVO> listMenus(String menuName) {
        List<SysPermission> menus = lambdaQuery().orderByAsc(SysPermission::getSort, SysPermission::getId).list();
        if (!StringUtils.hasText(menuName)) {
            return buildTree(menus);
        }
        String keyword = menuName.trim();
        Map<Long, SysPermission> menusById = menus.stream().collect(Collectors.toMap(SysPermission::getId, menu -> menu));
        Set<Long> includedMenuIds = new HashSet<>();
        for (SysPermission menu : menus) {
            if (!menu.getMenuName().contains(keyword)) {
                continue;
            }
            SysPermission current = menu;
            while (current != null && includedMenuIds.add(current.getId())) {
                current = menusById.get(current.getParentId());
            }
        }
        return buildTree(menus.stream().filter(menu -> includedMenuIds.contains(menu.getId())).toList());
    }

    /** 只返回被直接授予的有效页面和外链；祖先只补导航，不隐式授予任何操作。 */
    @Override
    public List<MenuVO> getCurrentMenus() {
        List<SysPermission> menus = lambdaQuery().eq(SysPermission::getStatus, 0)
                .ne(SysPermission::getMenuType, SysMenuType.BUTTON.getCode())
                .orderByAsc(SysPermission::getSort, SysPermission::getId).list();
        Set<Long> grantedMenuIds = authenticationContext.isSuperAdministrator()
                ? menus.stream().map(SysPermission::getId).collect(Collectors.toSet())
                : baseMapper.selectGrantedMenuIds(authenticationContext.getUserId());
        Map<Long, List<SysPermission>> menusByParent = menus.stream().collect(Collectors.groupingBy(SysPermission::getParentId));
        // 从顶级节点向下构建，停用父目录、断链及循环子树都不会进入导航。
        return buildNavigationTree(0L, menusByParent, grantedMenuIds);
    }

    /** 校验节点配置及父子关系后新增权限。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuSaveDTO request) {
        lockCatalog();
        SysPermission menu = prepareMenu(request);
        validateMenu(menu);
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
        SysPermission menu = prepareMenu(request);
        menu.setId(menuId);
        validateMenu(menu);
        boolean authorizationChanged = !Objects.equals(existing.getPerms(), menu.getPerms())
                || !Objects.equals(existing.getDataPermission(), menu.getDataPermission())
                || !Objects.equals(existing.getMenuType(), menu.getMenuType());
        boolean statusChanged = !Objects.equals(existing.getStatus(), menu.getStatus());
        if (existing.getBuiltIn() == 1 && authorizationChanged) {
            throw new IllegalArgumentException("内置节点的类型、权限标识和数据权限规则不能修改");
        }
        if (!authenticationContext.isSuperAdministrator()) {
            if (authorizationChanged || statusChanged) {
                throw new DataAccessDeniedException("只有超级管理员可以修改权限语义或操作状态");
            }
            authorizationService.requireGrantablePermission(menuId);
        }
        boolean protectedFieldsChanged = menu.getStatus() != 0 || menu.getVisible() != 0
                || !Objects.equals(existing.getParentId(), menu.getParentId());
        if (isProtectedMenu(existing) && protectedFieldsChanged) {
            throw new IllegalArgumentException("内置菜单管理入口及其操作不能停用、隐藏或迁移");
        }
        boolean revokeSessions = authorizationChanged || (!SysMenuType.DIRECTORY.matches(existing.getMenuType()) && statusChanged);
        Set<Long> affectedUserIds = revokeSessions
                ? baseMapper.selectUserIdSetByPermissionId(menuId) : Set.of();
        // 显式写入可空字段，类型切换或清空输入不能被 MyBatis-Plus 的非空更新策略忽略。
        update(menu, lambdaUpdate().eq(SysPermission::getId, menuId)
                .set(menu.getRouteName() == null, SysPermission::getRouteName, null)
                .set(menu.getPath() == null, SysPermission::getPath, null)
                .set(menu.getComponent() == null, SysPermission::getComponent, null)
                .set(menu.getParam() == null, SysPermission::getParam, null)
                .set(menu.getPerms() == null, SysPermission::getPerms, null)
                .set(menu.getIcon() == null, SysPermission::getIcon, null)
                .set(menu.getRemark() == null, SysPermission::getRemark, null).getWrapper());
        if (!affectedUserIds.isEmpty()) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
        }
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
        Set<Long> affectedUserIds = baseMapper.selectUserIdSetByPermissionId(permissionId);
        permission.setStatus(status);
        updateById(permission);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
    }

    /** 删除叶子节点并清理角色授权及操作数据范围，提交后撤销旧授权。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        lockCatalog();
        SysPermission menu = requiredMenu(menuId);
        if (isProtectedMenu(menu)) {
            throw new IllegalArgumentException("核心菜单管理入口及其操作不能删除");
        }
        if (lambdaQuery().eq(SysPermission::getParentId, menuId).exists()) {
            throw new IllegalArgumentException("请先删除所有子节点");
        }
        Set<Long> affectedUserIds = baseMapper.selectUserIdSetByPermissionId(menuId);
        permissionDeptService.lambdaUpdate().eq(SysRolePermissionDept::getPermissionId, menuId).remove();
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getPermissionId, menuId).remove();
        menu.setDeleted(1);
        removeById(menu);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
    }

    /** 核心管理节点必须保留，其他内置节点仍可执行删除。 */
    private boolean isProtectedMenu(SysPermission menu) {
        if (menu.getBuiltIn() != 1) {
            return false;
        }
        if (SysMenuType.DIRECTORY.matches(menu.getMenuType()) && menu.getParentId() == 0) {
            return true;
        }
        String permissionCode = menu.getPerms();
        return permissionCode != null && (permissionCode.startsWith("system:menu:")
                || permissionCode.startsWith("system:permission:"));
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
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在或已删除");
        }
        return menu;
    }

    /** 树结构写操作持有同一个事务锁，防止并发校验分别通过后形成循环。 */
    private void lockCatalog() {
        if (baseMapper.lockMenuCatalog() == null) {
            throw new IllegalStateException("菜单维护锁不存在，请检查数据库迁移");
        }
    }

    /** 校验标识唯一性，以及新增或修改后的层级关系。 */
    private void validateMenu(SysPermission menu) {
        Long menuId = menu.getId();
        if (menu.getPerms() != null && lambdaQuery().ne(menuId != null, SysPermission::getId, menuId)
                .eq(SysPermission::getPerms, menu.getPerms()).exists()) {
            throw new IllegalArgumentException("权限标识已存在");
        }
        // 使用迁移中已建立唯一索引的生成列，避免按路由字段扫描整表。
        if (menu.getRouteName() != null && query().ne(menuId != null, "id", menuId)
                .eq("active_route", menu.getRouteName()).exists()) {
            throw new IllegalArgumentException("路由名称已存在");
        }
        SysMenuType menuType = SysMenuType.fromCode(menu.getMenuType());
        if ((menuType == SysMenuType.DIRECTORY || menuType == SysMenuType.PAGE)
                && query().ne(menuId != null, "id", menuId).eq("active_path", menu.getPath()).exists()) {
            throw new IllegalArgumentException("路由地址已存在");
        }
        validateMenuHierarchy(menu);

        if (menuId != null) {
            var children = lambdaQuery().eq(SysPermission::getParentId, menuId);
            boolean invalidChildren = switch (menuType) {
                case DIRECTORY -> children.eq(SysPermission::getMenuType, SysMenuType.BUTTON.getCode()).exists();
                case PAGE -> children.ne(SysPermission::getMenuType, SysMenuType.BUTTON.getCode()).exists();
                case BUTTON, EXTERNAL_LINK -> children.exists();
            };
            if (invalidChildren) {
                throw new IllegalArgumentException("当前节点仍有不符合新类型的子节点");
            }
        }
    }

    /** 转换编辑参数，按节点类型校验并整理需要保存的字段。 */
    private SysPermission prepareMenu(MenuSaveDTO request) {
        SysPermission menu = BeanConvertUtils.convert(request, SysPermission.class);
        menu.setMenuName(menu.getMenuName().trim());
        menu.setPerms(StringUtils.hasText(menu.getPerms()) ? menu.getPerms().trim() : null);
        menu.setRouteName(StringUtils.hasText(menu.getRouteName()) ? menu.getRouteName().trim() : null);
        menu.setPath(StringUtils.hasText(menu.getPath()) ? menu.getPath().trim() : null);
        SysMenuType menuType = SysMenuType.fromCode(menu.getMenuType());
        if (menuType == SysMenuType.PAGE || menuType == SysMenuType.BUTTON) {
            if (menu.getPerms() == null || !menu.getPerms().matches("[a-z][A-Za-z0-9]*(?::[a-z][A-Za-z0-9]*){2,}")) {
                throw new IllegalArgumentException("页面和按钮必须填写精确权限标识，例如system:menu:list");
            }
        } else {
            menu.setPerms(null);
            menu.setDataPermission(0);
        }
        if (menuType == SysMenuType.DIRECTORY || menuType == SysMenuType.PAGE) {
            if (menu.getRouteName() == null || !menu.getRouteName().matches("[A-Za-z][A-Za-z0-9]*")
                    || Set.of("Layout", "UserProfile", "NoAccess", "NotFound", "login").contains(menu.getRouteName())) {
                throw new IllegalArgumentException("路由名称须由字母和数字组成且不能使用系统保留名称");
            }
            if (menu.getPath() == null || !menu.getPath().matches("/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*)(?:/(?:[a-z][A-Za-z0-9]*|:[a-z][A-Za-z0-9]*))*")
                    || menu.getPath().matches("/(?:login|profile|noAccess|redirect)(?:/.*)?")) {
                throw new IllegalArgumentException("路由须使用绝对路径及小驼峰片段，不能占用系统保留路径");
            }
            if (menu.getPath().contains("/:") && (menuType == SysMenuType.DIRECTORY || menu.getVisible() == 0)) {
                throw new IllegalArgumentException("带动态参数的路由只能用于隐藏页面，不能作为目录或可点击菜单");
            }
        } else {
            menu.setRouteName(null);
        }
        if (menuType == SysMenuType.PAGE) {
            if (!StringUtils.hasText(menu.getComponent()) || !menu.getComponent().matches("[A-Za-z][A-Za-z0-9]*(?:/[A-Za-z][A-Za-z0-9]*)*")) {
                throw new IllegalArgumentException("组件路径须相对于views目录，不含扩展名或路径跳转");
            }
            if (StringUtils.hasText(menu.getParam()) && (!menu.getParam().matches("(?:[A-Za-z][A-Za-z0-9]*=[^&#=]*)(?:&[A-Za-z][A-Za-z0-9]*=[^&#=]*)*")
                    || menu.getParam().contains("\\"))) {
                throw new IllegalArgumentException("路由参数须为key=value格式");
            }
        } else {
            menu.setComponent(null);
            menu.setParam(null);
            menu.setIsCache(0);
        }
        if (menuType == SysMenuType.BUTTON) {
            menu.setPath(null);
            menu.setIcon(null);
            menu.setVisible(0);
        }
        if (menuType == SysMenuType.EXTERNAL_LINK) {
            if (menu.getPath() == null) {
                throw new IllegalArgumentException("外链必须是有效的HTTP或HTTPS地址");
            }
            URI uri;
            try {
                uri = URI.create(menu.getPath());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("外链必须是有效的HTTP或HTTPS地址");
            }
            boolean httpLink = "https".equals(uri.getScheme()) || "http".equals(uri.getScheme());
            if (!httpLink || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("外链必须是有效的HTTP或HTTPS地址");
            }
        }
        return menu;
    }

    /** 校验父节点类型和完整祖先链，禁止形成循环或挂到停用目录下。 */
    private void validateMenuHierarchy(SysPermission menu) {
        SysPermission parent = menu.getParentId() == 0 ? null : getById(menu.getParentId());
        if (menu.getParentId() != 0 && parent == null) {
            throw new IllegalArgumentException("上级菜单不存在");
        }
        if (SysMenuType.BUTTON.matches(menu.getMenuType()) && (parent == null || !SysMenuType.PAGE.matches(parent.getMenuType()))) {
            throw new IllegalArgumentException("按钮权限必须属于页面菜单");
        }
        if (!SysMenuType.BUTTON.matches(menu.getMenuType()) && parent != null && !SysMenuType.DIRECTORY.matches(parent.getMenuType())) {
            throw new IllegalArgumentException("目录、页面和外链只能放在目录下");
        }
        Set<Long> ancestorIds = new HashSet<>();
        while (parent != null) {
            boolean pointsToSelf = Objects.equals(parent.getId(), menu.getId());
            if (pointsToSelf || !ancestorIds.add(parent.getId())) {
                throw new IllegalArgumentException("菜单不能移动到自身或后代节点下");
            }
            if (menu.getStatus() == 0 && parent.getStatus() != 0) {
                throw new IllegalArgumentException("启用菜单不能放在停用目录下");
            }
            if (parent.getParentId() == 0) {
                break;
            }
            parent = getById(parent.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("上级菜单的祖先链不完整");
            }
        }
    }

    /** 保留直接授权的页面和外链，目录仅在包含可访问子节点时返回。 */
    private List<MenuVO> buildNavigationTree(Long parentId, Map<Long, List<SysPermission>> menusByParent,
                                           Set<Long> grantedMenuIds) {
        List<MenuVO> navigation = new ArrayList<>();
        for (SysPermission menu : menusByParent.getOrDefault(parentId, List.of())) {
            if (!SysMenuType.DIRECTORY.matches(menu.getMenuType()) && !grantedMenuIds.contains(menu.getId())) {
                continue;
            }
            MenuVO node = toMenuVO(menu);
            if (SysMenuType.DIRECTORY.matches(menu.getMenuType())) {
                node.setChildren(buildNavigationTree(menu.getId(), menusByParent, grantedMenuIds));
                if (node.getChildren().isEmpty()) {
                    continue;
                }
            }
            navigation.add(node);
        }
        return navigation;
    }

    /** 按既定排序在线性时间组装树，查询结果中的节点不会重复展开。 */
    private List<MenuVO> buildTree(List<SysPermission> catalog) {
        Map<Long, MenuVO> nodes = new LinkedHashMap<>();
        for (SysPermission item : catalog) {
            nodes.put(item.getId(), toMenuVO(item));
        }
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO node : nodes.values()) {
            MenuVO parent = nodes.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}
