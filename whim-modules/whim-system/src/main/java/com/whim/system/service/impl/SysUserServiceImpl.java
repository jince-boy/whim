package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.core.auth.model.PostInfo;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysRoleDeptMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号查询及角色、权限信息构建。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final ISysRoleService roleService;
    private final ISysPermissionService permissionService;
    private final SysDeptMapper deptMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    /**
     * 通过id获取有效的用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public SysUser getUserById(Long id) {
        return this.lambdaQuery()
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                .eq(SysUser::getId, id)
                .one();
    }

    /**
     * 用户名全局规范化，登录阶段不依赖业务数据权限。
     */
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username.trim().toLowerCase(Locale.ROOT)).one();
    }

    /**
     * 构建用户、部门、岗位和角色授权快照，登录时一次性展开各角色的有效部门范围。
     */
    @Override
    public UserInfo buildUserInfo(SysUser user) {
        UserInfo info = BeanConvertUtils.convert(user, UserInfo.class);
        info.setLoginType(AuthUserType.SYSTEM);
        List<RoleInfo> roles = roleService.getRoleInfoListByUserId(user.getId());
        info.setRoleInfoList(roles);
        info.setRoleCodeSet(roles.stream().map(RoleInfo::getRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        info.setPermissionRoleIds(permissionService.getPermissionRoleIds(roles));
        info.setPermissionCodeSet(new LinkedHashSet<>(info.getPermissionRoleIds().keySet()));
        info.setMenuIdSet(permissionService.getMenuIdsByRoleIds(roles.stream().map(RoleInfo::getRoleId)
                .collect(Collectors.toSet())));

        List<PostInfo> posts = baseMapper.getPostInfoListByUserId(user.getId());
        Set<Long> departmentIds = new LinkedHashSet<>();
        departmentIds.add(user.getDeptId());
        for (PostInfo post : posts) {
            if (post.getDeptId() != null) {
                departmentIds.add(post.getDeptId());
            }
        }
        Set<Long> customRoleIds = new LinkedHashSet<>();
        boolean includeDescendants = false;
        for (RoleInfo role : roles) {
            info.getRoleDepartmentIds().put(role.getRoleId(), new LinkedHashSet<>());
            switch (DataScopeType.fromCode(role.getDataScope())) {
                case CUSTOM_DEPARTMENTS -> customRoleIds.add(role.getRoleId());
                case DEPARTMENT_AND_DESCENDANTS -> includeDescendants = true;
                case ALL, OWN_DEPARTMENT, SELF -> {
                    // 这些范围不需要额外加载角色部门关系或下级部门。
                }
            }
        }
        if (!customRoleIds.isEmpty()) {
            for (SysRoleDept relation : roleDeptMapper.getRoleDepartmentsByRoleIds(customRoleIds)) {
                info.getRoleDepartmentIds().get(relation.getRoleId()).add(relation.getDeptId());
                departmentIds.add(relation.getDeptId());
            }
        }

        // 主部门、岗位部门和自定义范围共用一次祖先链校验。
        Map<Long, DepartmentInfo> departments = deptMapper.getValidDepartmentsByIds(departmentIds).stream()
                .collect(Collectors.toMap(DepartmentInfo::getDeptId, department -> department));
        info.setDepartmentInfo(departments.get(user.getDeptId()));
        info.setPostInfoList(posts.stream()
                .filter(post -> post.getDeptId() == null || departments.containsKey(post.getDeptId())).toList());
        Set<Long> descendantIds = includeDescendants && info.getDepartmentInfo() != null
                ? deptMapper.getValidDepartmentAndDescendantIds(user.getDeptId()) : Set.of();
        for (RoleInfo role : roles) {
            Set<Long> roleDepartments = info.getRoleDepartmentIds().get(role.getRoleId());
            switch (DataScopeType.fromCode(role.getDataScope())) {
                case CUSTOM_DEPARTMENTS -> roleDepartments.retainAll(departments.keySet());
                case OWN_DEPARTMENT -> {
                    if (info.getDepartmentInfo() != null) {
                        roleDepartments.add(user.getDeptId());
                    }
                }
                case DEPARTMENT_AND_DESCENDANTS -> roleDepartments.addAll(descendantIds);
                case ALL, SELF -> {
                    // 全部及本人范围由角色范围类型表达，不依赖部门集合。
                }
            }
        }
        return info;
    }
}
