package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.core.auth.model.RoleInfo;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.dto.role.RoleSaveDTO;
import com.whim.system.model.vo.role.RoleVO;
import com.whim.system.model.vo.role.RoleDataScopeVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysRole业务服务。
 */
public interface ISysRoleService extends IService<SysRole> {
    /** 查询账号的有效角色。 */
    List<RoleInfo> getRoleInfoListByUserId(Long userId);
    /** 判断有效账号是否持有有效超级管理员角色。 */
    boolean isSuperAdministrator(Long userId);
    /** 查询系统角色目录。 */
    List<RoleVO> listRoles();
    /** 获取未删除角色，不返回跨域身份。 */
    SysRole getRequiredRole(Long roleId);
    /** 创建普通角色，默认仅本人范围。 */
    Long createRole(RoleSaveDTO request);
    /** 修改普通角色名称和编码。 */
    void updateRole(Long roleId, RoleSaveDTO request);
    /** 修改角色状态并撤销受影响会话。 */
    void setRoleStatus(Long roleId, Integer status);
    /** 查询默认数据范围及自定义部门。 */
    RoleDataScopeVO getDataScope(Long roleId);
    /** 覆盖默认数据范围及自定义部门。 */
    void replaceDataScope(Long roleId, Integer dataScope, Set<Long> deptIds);
    /** 默认范围与操作覆盖范围共用参数及有效部门校验。 */
    void validateDataScope(Integer dataScope, Set<Long> deptIds);
    /** 删除未分配给用户的普通角色。 */
    void deleteRole(Long roleId);
}

