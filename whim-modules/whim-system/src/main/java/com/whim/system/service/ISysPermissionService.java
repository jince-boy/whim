package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.dto.permission.SysMenuQueryDTO;
import com.whim.system.model.dto.permission.SysMenuSaveDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.permission.SysMenuTreeVO;
import com.whim.system.model.vo.permission.SysMenuVO;
import com.whim.system.model.vo.permission.SysRoleMenuTreeVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 全局菜单权限维护及登录授权查询，不按部门或创建人隔离。
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * 根据id查询菜单详情。
     *
     * @param id 菜单id
     * @return 菜单详情
     */
    SysMenuVO getMenuDetail(Long id);

    /**
     * 查询菜单列表，按名称筛选时保留祖先和匹配节点的子树。
     *
     * @param query 查询参数
     * @return 菜单树列表
     */
    List<SysMenuVO> getMenuList(SysMenuQueryDTO query);

    /**
     * 根据登录上下文中的菜单授权获取动态路由，不要求菜单配置权限标识。
     *
     * @return 保留隐藏页面、排除按钮和停用分支的路由菜单树
     */
    List<SysMenuVO> getUserMenus();

    /**
     * 获取菜单下拉树列表。
     *
     * @return 包含按钮和停用节点的菜单树列表
     */
    List<SysMenuTreeVO> getMenuTreeList();

    /**
     * 获取角色菜单授权树。
     *
     * @param roleId 角色id
     * @return 完整菜单树和角色已授权的菜单id
     */
    SysRoleMenuTreeVO getRoleMenuTree(Long roleId);

    /**
     * 登录时查询角色关联的菜单id，供路由查询使用。
     *
     * @param roleIds 角色id集合
     * @return 角色直接授予的菜单id集合
     */
    Set<Long> getMenuIdsByRoleIds(Set<Long> roleIds);

    /**
     * 根据权限码查询有效权限。
     *
     * @param permissionCode 精确权限码
     * @return 有效的页面或按钮权限，不存在时返回null
     */
    SysPermission getPermissionByCode(String permissionCode);

    /**
     * 构建权限码与授权角色的对应关系，供功能权限和数据权限共用。
     *
     * @param roles 登录时已核验的有效角色列表
     * @return 权限码与授权角色id集合的对应关系
     */
    Map<String, Set<Long>> getPermissionRoleIds(List<RoleInfo> roles);

    /**
     * 新增菜单权限，仅允许超级管理员操作。
     *
     * @param parameter 菜单新增参数
     * @return 新增菜单id
     */
    Long addMenu(SysMenuSaveDTO parameter);

    /**
     * 修改菜单权限，授权变化在事务提交后使相关会话失效。
     *
     * @param id 菜单id
     * @param parameter 菜单修改参数
     */
    void updateMenu(Long id, SysMenuSaveDTO parameter);

    /**
     * 删除没有子节点的菜单并清理角色授权，内置首页和核心菜单管理节点不能删除。
     *
     * @param id 菜单id
     */
    void deleteMenu(Long id);
}
