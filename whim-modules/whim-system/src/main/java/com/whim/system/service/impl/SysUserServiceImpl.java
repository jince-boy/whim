package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.DepartmentInfo;
import com.whim.core.auth.model.PostInfo;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.enums.DataScopeType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.mapper.SysPostMapper;
import com.whim.system.mapper.SysUserMapper;
import com.whim.system.model.dto.post.SysPostMemberQueryDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.entity.SysUserRole;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.post.SysPostMemberVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.system.service.ISysRoleService;
import com.whim.system.service.ISysUserRoleService;
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
    private final ISysUserRoleService userRoleService;
    private final SysDeptMapper deptMapper;
    private final SysPostMapper postMapper;

    /**
     * 分页查询岗位已分配成员，供岗位服务校验岗位访问权限后调用。
     *
     * @param query 岗位ID及用户筛选条件
     * @return 当前成员查询权限范围内的用户分页
     */
    @Override
    @DataPermission(permission = "system:post:member:list", tables =
            @DataPermissionTable(name = "sys_user", userColumn = "id"))
    public PageDataVO<SysPostMemberVO> getPostMemberPage(SysPostMemberQueryDTO query) {
        return new PageDataVO<>(baseMapper.getPostMemberPage(query.getPage(), query));
    }

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
     * 按忽略大小写的用户名查询账号，登录阶段不依赖业务数据权限。
     *
     * @param username 用户名
     * @return 用户账号，不存在时返回 null
     */
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username.toLowerCase(Locale.ROOT)).one();
    }

    /**
     * 查询部门移动或启停影响的主部门用户、岗位成员和自定义数据范围角色用户。
     *
     * @param departmentIds 发生变化的非空部门ID集合
     * @return 需要重新加载部门授权信息的用户ID集合
     */
    @Override
    public Set<Long> getAffectedUserIdsByDepartments(Set<Long> departmentIds) {
        // 会话失效必须覆盖所有受影响用户，Mapper 的内部只读查询不受操作者可见范围限制。
        return baseMapper.getAffectedUserIdsByDepartments(departmentIds);
    }

    /**
     * 内部查询祖先部门中拥有本部门及下级范围角色的用户，用于组织变更后的会话失效。
     *
     * @param ancestorIds 受影响的祖先部门ID集合，不包含根标识0
     * @return 主部门位于指定祖先中且拥有启用的下级范围角色的未删除用户ID集合
     */
    @Override
    public Set<Long> getUserIdsByDepartmentAncestors(Set<Long> ancestorIds) {
        if (ancestorIds.isEmpty()) {
            return Set.of();
        }
        // 会话失效要覆盖所有受影响用户，不能被操作者的可见范围截断；这里仅执行内部只读查询。
        return DataPermissionContext.getMetadata(() -> {
            List<Long> userIds = this.lambdaQuery().select(SysUser::getId)
                    .in(SysUser::getDeptId, ancestorIds).list().stream().map(SysUser::getId).toList();
            if (userIds.isEmpty()) {
                return Set.of();
            }
            List<SysUserRole> userRoles = userRoleService.lambdaQuery()
                    .select(SysUserRole::getUserId, SysUserRole::getRoleId)
                    .in(SysUserRole::getUserId, userIds).list();
            if (userRoles.isEmpty()) {
                return Set.of();
            }
            // 批量筛选这些用户实际持有的角色；一人有多个符合条件的角色时，只使其会话失效一次。
            Set<Long> roleIds = roleService.lambdaQuery().select(SysRole::getId)
                    .in(SysRole::getId, userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet()))
                    .eq(SysRole::getStatus, 0)
                    .eq(SysRole::getDataScope, DataScopeType.DEPARTMENT_AND_DESCENDANTS.getCode())
                    .list().stream().map(SysRole::getId).collect(Collectors.toSet());
            return userRoles.stream().filter(relation -> roleIds.contains(relation.getRoleId()))
                    .map(SysUserRole::getUserId).collect(Collectors.toSet());
        });
    }

    /**
     * 构建用户、部门、岗位和角色授权快照，登录时一次性展开各角色的有效部门范围。
     *
     * @param user 登录用户
     * @return 包含部门、岗位和角色授权范围的登录信息
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

        List<PostInfo> posts = postMapper.getPostInfoListByUserId(user.getId());
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
            for (SysRoleDept relation : baseMapper.getRoleDepartmentsByRoleIds(customRoleIds)) {
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
