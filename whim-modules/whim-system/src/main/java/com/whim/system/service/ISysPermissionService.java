package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.menu.MenuSaveDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.menu.MenuVO;
import com.whim.system.model.vo.permission.PermissionVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统权限目录与授权校验。
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /** 查询单个菜单或按钮权限详情。 */
    MenuVO getMenu(Long menuId);
    /** 查询账号的有效操作权限码集合。 */
    Set<String> getPermissionCodeSetByUserId(Long userId);
    /** 查询系统权限目录。 */
    List<PermissionVO> listPermissions();
    /** 查询包含目录、页面、按钮和外链的管理树。 */
    List<MenuVO> listMenus(String menuName);
    /** 查询当前用户直接获得授权的导航菜单。 */
    List<MenuVO> getCurrentMenus();
    /** 新增菜单或按钮权限。 */
    Long createMenu(MenuSaveDTO request);
    /** 更新完整菜单配置。 */
    void updateMenu(Long menuId, MenuSaveDTO request);
    /** 修改权限状态并在事务提交后撤销相关会话。 */
    void setPermissionStatus(Long permissionId, Integer status);
    /** 删除叶子节点，并清理角色授权和操作数据范围。 */
    void deleteMenu(Long menuId);
    /** 校验权限有效且在操作者可委派范围内，返回权限供授权配置复用。 */
    SysPermission requireGrantablePermission(Long permissionId);
    /** 在调用方事务中锁定菜单目录，串行协调菜单维护与角色授权。 */
    void lockCatalog();
}

