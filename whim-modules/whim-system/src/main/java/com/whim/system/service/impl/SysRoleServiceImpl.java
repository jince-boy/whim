package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysRoleMapper;
import com.whim.system.model.dto.role.SysRoleAddDTO;
import com.whim.system.model.dto.role.SysRoleDataScopeDTO;
import com.whim.system.model.dto.role.SysRoleQueryDTO;
import com.whim.system.model.dto.role.SysRoleStatusDTO;
import com.whim.system.model.dto.role.SysRoleUpdateDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysRolePermission;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.vo.role.SysRoleDataScopeVO;
import com.whim.system.model.vo.role.SysRoleVO;
import com.whim.system.service.ISysRoleDeptService;
import com.whim.system.service.ISysRolePermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 全局角色基础资料及统一数据范围管理，操作由菜单权限码授权。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    private final ISysRoleDeptService roleDeptService;
    private final ISysRolePermissionService rolePermissionService;
    private final ISysUserRoleService userRoleService;
    private final AuthenticationSession authenticationSession;
    private final SysDeptMapper deptMapper;

    /**
     * 根据名称、编码和状态分页查询所有未删除角色。
     *
     * @param query 筛选及分页条件
     * @return 按排序值和ID排列的角色分页
     */
    @Override
    public PageDataVO<SysRoleVO> getRolePage(SysRoleQueryDTO query) {
        Page<SysRole> roles = this.lambdaQuery()
                .like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                .like(StringUtils.hasText(query.getRoleCode()), SysRole::getRoleCode, query.getRoleCode())
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
                .orderByAsc(SysRole::getSort, SysRole::getId).page(query.getPage());
        return new PageDataVO<>(roles.convert(role -> BeanConvertUtils.convert(role, SysRoleVO.class)));
    }

    /**
     * 查询角色基础资料。
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Override
    public SysRoleVO getRoleDetail(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return BeanConvertUtils.convert(role, SysRoleVO.class);
    }

    /**
     * 查询角色统一数据范围及实际选择的部门，不展开下级部门。
     *
     * @param id 角色ID
     * @return 数据范围详情
     */
    @Override
    @Transactional(readOnly = true)
    public SysRoleDataScopeVO getRoleDataScope(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        SysRoleDataScopeVO result = new SysRoleDataScopeVO();
        result.setId(id);
        result.setDataScope(role.getDataScope());
        result.setDeptIds(roleDeptService.lambdaQuery().eq(SysRoleDept::getRoleId, id)
                .list().stream().map(SysRoleDept::getDeptId).toList());
        return result;
    }

    /**
     * 查询账号当前正常且未删除的角色。
     *
     * @param userId 用户ID
     * @return 当前有效角色
     */
    @Override
    public List<RoleInfo> getRoleInfoListByUserId(Long userId) {
        return baseMapper.getRoleInfoListByUserId(userId);
    }

    /**
     * 新增角色，数据范围从本人开始，权限在独立入口分配。
     *
     * @param parameter 角色基础资料
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRoleAddDTO parameter) {
        SysRole role = BeanConvertUtils.convert(parameter, SysRole.class);
        validateRoleCode(role);
        role.setDataScope(DataScopeType.SELF.getCode());
        if (!this.save(role)) {
            throw new ServiceException("新增角色失败");
        }
    }

    /**
     * 修改基础资料，状态与数据范围不从本请求接收。
     *
     * @param parameter 角色ID及基础资料
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRoleUpdateDTO parameter) {
        SysRole original = getManagedRole(parameter.getId());
        SysRole role = BeanConvertUtils.convert(parameter, SysRole.class);
        validateRoleCode(role);
        if (!this.updateById(role)) {
            throw new ServiceException("修改角色失败");
        }
        // 名称和编码进入登录快照，排序与备注变化不需要用户重新登录。
        if (!original.getRoleName().equals(role.getRoleName()) || !original.getRoleCode().equals(role.getRoleCode())) {
            invalidateRoleSessions(role.getId());
        }
    }

    /**
     * 修改角色状态，提交后使所有关联用户的旧授权失效。
     *
     * @param parameter 角色ID及目标状态
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(SysRoleStatusDTO parameter) {
        SysRole role = getManagedRole(parameter.getId());
        if (role.getStatus().equals(parameter.getStatus())) {
            return;
        }
        // 局部变更保留完整记录中的备注等可空字段，审计字段仍由框架自动填充。
        role.setStatus(parameter.getStatus());
        if (!this.updateById(role)) {
            throw new ServiceException("修改角色状态失败");
        }
        invalidateRoleSessions(role.getId());
    }

    /**
     * 保存角色统一数据范围和自定义部门，所有操作共用同一个范围。
     *
     * @param parameter 角色ID、数据范围及完整部门集合
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleDataScope(SysRoleDataScopeDTO parameter) {
        SysRole role = getManagedRole(parameter.getId());
        Set<Long> requested = new LinkedHashSet<>(parameter.getDeptIds());
        if (parameter.getDataScope() == DataScopeType.CUSTOM_DEPARTMENTS.getCode()) {
            if (requested.isEmpty()) {
                throw new ServiceException("自定义数据范围至少选择一个部门");
            }
            // 复用登录时的批量祖先链校验；组织锁防止验证后部门被移动、停用或删除。
            if (deptMapper.getValidDepartmentsByIds(requested).size() != requested.size()) {
                throw new ServiceException("自定义部门不存在、已停用或上级部门无效，请重新选择");
            }
        } else if (!requested.isEmpty()) {
            throw new IllegalArgumentException("非自定义数据范围不能提交部门集合");
        }
        Set<Long> current = roleDeptService.lambdaQuery().eq(SysRoleDept::getRoleId, role.getId())
                .list().stream().map(SysRoleDept::getDeptId).collect(Collectors.toSet());
        if (role.getDataScope().equals(parameter.getDataScope()) && current.equals(requested)) {
            return;
        }
        role.setDataScope(parameter.getDataScope());
        if (!this.updateById(role)) {
            throw new ServiceException("保存角色数据范围失败");
        }
        // 切换范围时原自定义部门必须一起清理，防止以后切回时恢复过期授权。
        roleDeptService.lambdaUpdate().eq(SysRoleDept::getRoleId, role.getId()).remove();
        if (!requested.isEmpty()) {
            List<SysRoleDept> relations = requested.stream().map(deptId -> {
                SysRoleDept relation = new SysRoleDept();
                relation.setRoleId(role.getId());
                relation.setDeptId(deptId);
                return relation;
            }).toList();
            if (!roleDeptService.saveBatch(relations)) {
                throw new ServiceException("保存角色自定义部门失败");
            }
        }
        invalidateRoleSessions(role.getId());
    }

    /**
     * 删除无成员关联的角色，并在同一事务中清理授权和数据范围。
     *
     * @param id 角色ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        getManagedRole(id);
        // 全量检查用户关联，不能遗漏停用账号或当前操作者看不到的用户。
        if (userRoleService.lambdaQuery().eq(SysUserRole::getRoleId, id).exists()) {
            throw new ServiceException("角色仍被用户使用，请先在用户管理中调整角色");
        }
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getRoleId, id).remove();
        roleDeptService.lambdaUpdate().eq(SysRoleDept::getRoleId, id).remove();
        if (!this.removeById(id)) {
            throw new ServiceException("删除角色失败");
        }
    }

    /**
     * 读取目标角色并检查记录存在性，不区分角色编码或初始化ID。
     *
     * @param id 角色ID
     * @return 可维护的角色记录
     */
    private SysRole getManagedRole(Long id) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return role;
    }

    /**
     * 校验未删除角色之间的编码唯一性。
     *
     * @param role 待保存角色
     */
    private void validateRoleCode(SysRole role) {
        if (this.lambdaQuery().eq(SysRole::getRoleCode, role.getRoleCode())
                .ne(role.getId() != null, SysRole::getId, role.getId()).exists()) {
            throw new ServiceException("角色编码已存在");
        }
    }

    /**
     * 在事务提交后失效所有关联用户的授权快照，不按用户可见范围裁剪。
     *
     * @param roleId 已变更角色ID
     */
    private void invalidateRoleSessions(Long roleId) {
        List<Long> userIds = userRoleService.lambdaQuery().select(SysUserRole::getUserId)
                .eq(SysUserRole::getRoleId, roleId).list().stream().map(SysUserRole::getUserId).toList();
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, userIds);
    }
}
