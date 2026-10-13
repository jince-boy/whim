package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.menu.MenuSaveDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.menu.MenuVO;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 共用权限表的菜单导航与目录维护服务。
 */
public interface ISysMenuService extends IService<SysPermission> {
    /** 查询完整管理树，搜索同时保留匹配节点祖先。 */
    List<MenuVO> listMenus(String menuName);
    /** 查询可编辑节点详情。 */
    MenuVO getMenu(Long menuId);
    /** 返回当前用户直接获得的页面、外链及所需有效祖先。 */
    List<MenuVO> getCurrentMenus();
    /** 创建节点，校验父子类型、路由和权限语义。 */
    Long createMenu(MenuSaveDTO request);
    /** 修改节点，保护内置定义并串行校验树迁移。 */
    void updateMenu(Long menuId, MenuSaveDTO request);
    /** 删除没有子节点和角色引用的自定义节点。 */
    void deleteMenu(Long menuId);
}
