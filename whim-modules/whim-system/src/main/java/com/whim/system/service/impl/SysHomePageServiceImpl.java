package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysHomePageMapper;
import com.whim.system.model.dto.homepage.HomeTargetQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageAddDTO;
import com.whim.system.model.dto.homepage.SysHomePageQueryDTO;
import com.whim.system.model.dto.homepage.SysHomePageUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysHomePage;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysMenuType;
import com.whim.system.model.vo.homepage.HomeRouteVO;
import com.whim.system.model.vo.homepage.HomeTargetVO;
import com.whim.system.model.vo.homepage.SysHomePageVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysHomePageService;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 组织首页配置及登录账号匹配。
 */
@Service
@RequiredArgsConstructor
public class SysHomePageServiceImpl extends ServiceImpl<SysHomePageMapper, SysHomePage> implements ISysHomePageService {
    private static final int USER = 1;
    private static final int ROLE = 2;
    private static final int DEPARTMENT = 3;
    private static final int GLOBAL = 4;
    private static final Set<String> FIXED_PATHS = Set.of("/login", "/profile", "/noaccess", "/redirect");
    private static final Set<String> FIXED_VIEW_DIRECTORIES = Set.of("login", "profile", "error", "unavailable");

    private final AuthenticationContext authenticationContext;
    private final ISysUserService users;
    private final ISysRoleService roles;
    private final ISysDeptService departments;
    private final ISysPermissionService permissions;

    /**
     * 分页查询首页配置。
     *
     * @param query 分页和筛选条件
     * @return 首页配置分页
     */
    @Override
    public PageDataVO<SysHomePageVO> getHomePageConfigPage(SysHomePageQueryDTO query) {
        return new PageDataVO<>(baseMapper.getHomePageConfigPage(query.getPage(), query));
    }

    /**
     * 搜索可关联的用户、角色或部门。
     *
     * @param query 类型、关键词和分页条件
     * @return 关联对象分页
     */
    @Override
    public PageDataVO<HomeTargetVO> getHomeTargetPage(HomeTargetQueryDTO query) {
        String keyword = StringUtils.trimToNull(query.getKeyword());
        boolean search = keyword != null;
        IPage<HomeTargetVO> targets = switch (query.getTargetType()) {
            case USER -> users.lambdaQuery().select(SysUser::getId, SysUser::getName, SysUser::getUsername)
                    .eq(SysUser::getStatus, 0)
                    .and(search, condition -> condition.like(SysUser::getName, keyword).or().like(SysUser::getUsername, keyword))
                    .orderByAsc(SysUser::getUsername, SysUser::getId).page(query.getPage())
                    .convert(user -> new HomeTargetVO(user.getId(), user.getUsername() + "（" + user.getName() + "）"));
            case ROLE -> roles.lambdaQuery().select(SysRole::getId, SysRole::getRoleName)
                    .eq(SysRole::getStatus, 0).like(search, SysRole::getRoleName, keyword)
                    .orderByAsc(SysRole::getRoleName, SysRole::getId).page(query.getPage())
                    .convert(role -> new HomeTargetVO(role.getId(), role.getRoleName()));
            case DEPARTMENT -> departments.lambdaQuery().select(SysDept::getId, SysDept::getDeptName)
                    .eq(SysDept::getStatus, 0).like(search, SysDept::getDeptName, keyword)
                    .orderByAsc(SysDept::getDeptName, SysDept::getId).page(query.getPage())
                    .convert(department -> new HomeTargetVO(department.getId(), department.getDeptName()));
            default -> throw new ServiceException("关联类型无效");
        };
        return new PageDataVO<>(targets);
    }

    /**
     * 按优先顺序查询当前账号可匹配的首页。
     *
     * @return 首页路由候选
     */
    @Override
    public List<HomeRouteVO> getCurrentHomeRoutes() {
        UserInfo user = authenticationContext.getCurrentUserInfo();
        List<Long> roleIds = user.getRoleInfoList().stream().map(RoleInfo::getRoleId).toList();
        Long departmentId = user.getDepartmentInfo() == null ? null : user.getDepartmentInfo().getDeptId();
        // 使用登录快照中的有效角色和主部门，已绑定菜单仍须由当前账号获得授权。
        return baseMapper.getCurrentHomeRoutes(user.getUserId(), roleIds, departmentId).stream()
                .filter(route -> route.getMenuId() == null || user.isSuperAdministrator() || user.getMenuIdSet().contains(route.getMenuId()))
                .toList();
    }

    /**
     * 新增首页配置。
     *
     * @param parameter 首页配置
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void addHomePage(SysHomePageAddDTO parameter) {
        SysHomePage home = BeanConvertUtils.convert(parameter, SysHomePage.class);
        validateHomePage(home, null);
        save(home);
    }

    /**
     * 更新首页配置。
     *
     * @param parameter 首页配置和主键
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void updateHomePage(SysHomePageUpdateDTO parameter) {
        SysHomePage home = BeanConvertUtils.convert(parameter, SysHomePage.class);
        SysHomePage original = getById(home.getId());
        if (original == null) {
            throw new ServiceException("首页配置不存在");
        }
        validateHomePage(home, original);
        updateById(home);
    }

    /**
     * 删除首页配置。
     *
     * @param id 配置主键
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void deleteHomePage(Long id) {
        if (!removeById(id)) {
            throw new ServiceException("首页配置不存在");
        }
    }

    /**
     * 校验关联对象、组件及路由占用情况，并绑定相符的现有菜单。
     *
     * @param home 待保存的首页配置
     * @param original 修改前的配置，新增时为空
     */
    private void validateHomePage(SysHomePage home, SysHomePage original) {
        Long targetId = home.getTargetId();
        // 全局默认使用统一的业务键，联合唯一索引同时约束并发写入和删除后重建。
        if (home.getTargetType() == GLOBAL) {
            if (targetId != null && targetId != 0L) {
                throw new ServiceException("全局默认无需指定关联对象");
            }
            home.setTargetId(0L);
        }
        boolean sameTarget = original != null && original.getTargetType().equals(home.getTargetType())
                && original.getTargetId().equals(home.getTargetId());
        if (home.getTargetType() != GLOBAL && (!sameTarget || home.getStatus() == 0)) {
            // 已删除或停用的对象仍允许停用原配置，但不能作为新的关联对象。
            boolean exists = targetId != null && switch (home.getTargetType()) {
                case USER -> users.lambdaQuery().eq(SysUser::getId, targetId).eq(SysUser::getStatus, 0).exists();
                case ROLE -> roles.lambdaQuery().eq(SysRole::getId, targetId).eq(SysRole::getStatus, 0).exists();
                case DEPARTMENT -> departments.lambdaQuery().eq(SysDept::getId, targetId).eq(SysDept::getStatus, 0).exists();
                default -> false;
            };
            if (!exists) {
                throw new ServiceException("关联对象不存在或已停用");
            }
        }
        if (!sameTarget && lambdaQuery().eq(SysHomePage::getTargetType, home.getTargetType())
                .eq(SysHomePage::getTargetId, home.getTargetId()).exists()) {
            throw new ServiceException("该关联对象已配置首页，请修改已有配置");
        }
        String path = home.getPath();
        String lowerPath = path.toLowerCase(Locale.ROOT);
        if (FIXED_PATHS.contains(lowerPath) || lowerPath.startsWith("/redirect/")) {
            throw new ServiceException("首页路由与系统固定路由冲突");
        }
        String component = normalizeComponent(home.getComponent());
        if (StringUtils.containsAny(component, '\\', '?', '#')
                || Arrays.stream(component.split("/", -1)).anyMatch(part -> part.isBlank() || ".".equals(part) || "..".equals(part))
                || FIXED_VIEW_DIRECTORIES.contains(StringUtils.substringBefore(component, "/"))) {
            throw new ServiceException("请输入相对于 src/views 的业务页面组件路径");
        }
        home.setComponent(component);
        // 路由大小写按浏览器路由匹配处理，组件文件名则必须精确匹配。
        List<SysHomePage> samePath = lambdaQuery().select(SysHomePage::getPath, SysHomePage::getComponent)
                .eq(SysHomePage::getPath, path).ne(home.getId() != null, SysHomePage::getId, home.getId())
                .groupBy(SysHomePage::getPath, SysHomePage::getComponent).list();
        if (samePath.stream().anyMatch(other -> !path.equals(other.getPath()) || !component.equals(other.getComponent()))) {
            throw new ServiceException("该首页路由已对应其他组件或路径大小写不一致");
        }
        String relativePath = path.substring(1);
        SysPermission menu = permissions.lambdaQuery()
                .select(SysPermission::getId, SysPermission::getPath, SysPermission::getMenuType, SysPermission::getComponent)
                .in(SysPermission::getPath, path, relativePath).one();
        if (menu != null) {
            if (!(path.equals(menu.getPath()) || relativePath.equals(menu.getPath()))
                    || !SysMenuType.PAGE.getCode().equals(menu.getMenuType())
                    || !component.equals(normalizeComponent(menu.getComponent()))) {
                throw new ServiceException("首页路由与已有菜单冲突，请使用该菜单的页面组件或更换路由");
            }
            home.setMenuId(menu.getId());
        } else if (original != null && path.equals(original.getPath())) {
            // 修改优先级或状态不能解除旧菜单的授权要求；换到新地址才重新绑定。
            home.setMenuId(original.getMenuId());
        }
    }

    /**
     * 统一已有首页标识及可省略的组件扩展名。
     *
     * @param value 组件路径
     * @return 规范化组件路径
     */
    private String normalizeComponent(String value) {
        String path = StringUtils.trimToEmpty(value);
        if (path.endsWith(".vue")) {
            path = path.substring(0, path.length() - 4);
        }
        return "layout/default/index".equals(path) ? "home/index" : path;
    }
}
