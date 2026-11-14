package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.role.SysRoleAddDTO;
import com.whim.system.model.dto.role.SysRoleDataScopeDTO;
import com.whim.system.model.dto.role.SysRoleQueryDTO;
import com.whim.system.model.dto.role.SysRoleStatusDTO;
import com.whim.system.model.dto.role.SysRoleUpdateDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.vo.role.SysRoleDataScopeVO;
import com.whim.system.model.vo.role.SysRoleVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 全局角色管理及登录账号的有效角色查询。
 */
public interface ISysRoleService extends IService<SysRole> {
    /**
     * 根据名称、编码和状态分页查询所有未删除角色。
     *
     * @param query 筛选及分页条件
     * @return 角色分页
     */
    PageDataVO<SysRoleVO> getRolePage(SysRoleQueryDTO query);

    /**
     * 查询角色基础资料。
     *
     * @param id 角色ID
     * @return 角色详情
     */
    SysRoleVO getRoleDetail(Long id);

    /**
     * 查询角色统一数据范围和实际选择的部门。
     *
     * @param id 角色ID
     * @return 数据范围详情
     */
    SysRoleDataScopeVO getRoleDataScope(Long id);

    /**
     * 查询账号的有效角色。
     *
     * @param userId 用户ID
     * @return 正常且未删除的角色
     */
    List<RoleInfo> getRoleInfoListByUserId(Long userId);

    /**
     * 新增角色，初始数据范围为本人。
     *
     * @param parameter 角色基础资料
     */
    void addRole(SysRoleAddDTO parameter);

    /**
     * 修改角色基础资料，保留状态和授权范围。
     *
     * @param parameter 角色ID及基础资料
     */
    void updateRole(SysRoleUpdateDTO parameter);

    /**
     * 修改角色状态并在提交后失效关联用户会话。
     *
     * @param parameter 角色ID及目标状态
     */
    void updateRoleStatus(SysRoleStatusDTO parameter);

    /**
     * 保存角色统一数据范围及自定义部门。
     *
     * @param parameter 角色ID、数据范围及完整部门集合
     */
    void assignRoleDataScope(SysRoleDataScopeDTO parameter);

    /**
     * 删除没有用户关联的角色并清理授权关系。
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);
}
