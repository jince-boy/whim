package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.sysPermission.SysPermissionInsertDTO;
import com.whim.system.model.dto.sysPermission.SysPermissionQueryDTO;
import com.whim.system.model.entity.SysPermission;
import com.whim.system.model.vo.MenuVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2024-10-23 19:53:58
 * @description 菜单权限(SysPermission)表服务接口
 */
public interface ISysPermissionService extends IService<SysPermission> {
    /**
     * 根据用户id获取权限列表
     *
     * @param id 用户id
     * @return 权限列表
     */
    Set<String> getPermissionList(Long id);

    /**
     * 根据用户id获取菜单树
     *
     * @param id 用户id
     * @return 菜单树
     */
    List<MenuVO> getMenuTreeByUserId(Long id);

    /**
     * 获取所有菜单树
     *
     * @param sysPermissionQueryDTO 查询参数
     * @return 菜单树
     */
    List<MenuVO> getAllMenuThree(SysPermissionQueryDTO sysPermissionQueryDTO);

    /**
     * 添加菜单
     *
     * @param sysPermissionInsertDTO 菜单参数
     * @return 添加结果 true:添加成功 false:添加失败
     */
    Boolean insertPermission(SysPermissionInsertDTO sysPermissionInsertDTO);
}
