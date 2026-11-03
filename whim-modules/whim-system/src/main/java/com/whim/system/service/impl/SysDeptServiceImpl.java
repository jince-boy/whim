package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.ValueParserUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptQueryDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.dept.SysDeptDetailVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    private final AuthenticationSession authenticationSession;
    private final ISysUserService userService;
    private final ISysPostService postService;

    /**
     * 分页查询可见部门内的负责人候选用户。
     *
     * @param query 部门ID、分页及搜索参数
     * @return 本部门的负责人候选用户
     */
    @Override
    @DataPermission(permission = "system:dept:list", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public PageDataVO<SysDeptLeaderVO> getDeptLeaderOptions(SysDeptLeaderQueryDTO query) {
        if (!this.lambdaQuery().eq(SysDept::getId, query.getDeptId()).exists()) {
            throw new ServiceException("部门不存在或无权查看");
        }
        String keyword = query.getKeyword();
        Page<SysUser> users = userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getName, SysUser::getUsername)
                .eq(SysUser::getDeptId, query.getDeptId())
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysUser::getName, keyword).or().like(SysUser::getUsername, keyword))
                .orderByAsc(SysUser::getId)
                .page(query.getPage());
        return new PageDataVO<>(users.convert(user -> BeanConvertUtils.convert(user, SysDeptLeaderVO.class)));
    }

    /**
     * 查询可修改的部门详情及负责人信息。
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Override
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public SysDeptDetailVO getDeptDetail(Long id) {
        SysDept department = this.getById(id);
        if (department == null) {
            throw new ServiceException("部门不存在或无权修改");
        }
        SysDeptDetailVO detail = BeanConvertUtils.convert(department, SysDeptDetailVO.class);
        if (department.getLeaderUserId() != null) {
            SysUser leader = userService.getUserById(department.getLeaderUserId());
            if (leader != null) {
                detail.setLeaderName(leader.getName());
                detail.setLeaderUsername(leader.getUsername());
            }
        }
        return detail;
    }

    /**
     * 按名称和状态查询可见部门树。
     *
     * @param query 部门名称和状态查询条件
     * @return 按排序值和ID升序排列的部门树
     */
    @Override
    @DataPermission(permission = "system:dept:list", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public List<SysDeptVO> getDeptList(SysDeptQueryDTO query) {
        List<SysDeptVO> departments = BeanConvertUtils.convertList(this.lambdaQuery()
                .like(StringUtils.hasText(query.getDeptName()), SysDept::getDeptName, query.getDeptName())
                .eq(query.getStatus() != null, SysDept::getStatus, query.getStatus())
                .orderByAsc(SysDept::getSort, SysDept::getId)
                .list(), SysDeptVO.class);
        Map<Long, SysDeptVO> departmentMap = departments.stream()
                .collect(Collectors.toMap(SysDeptVO::getId, department -> department));
        List<SysDeptVO> roots = new ArrayList<>();
        for (SysDeptVO department : departments) {
            SysDeptVO parent = departmentMap.get(department.getParentId());
            if (parent == null) {
                // 上级未匹配或不可见时，当前部门作为根节点。
                roots.add(department);
            } else {
                parent.getChildren().add(department);
            }
        }
        return roots;
    }

    /**
     * 新增部门，提交后使受影响的授权会话失效。
     *
     * @param parameter 部门新增参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    public void addDept(SysDeptAddDTO parameter) {
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        department.setAncestors("0");
        if (department.getParentId() != 0L) {
            SysDept parent = this.getById(department.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在");
            }
            department.setAncestors(parent.getAncestors() + "," + parent.getId());
        }

        if (!this.save(department)) {
            throw new ServiceException("新增部门失败");
        }
        invalidateAncestorSessions(department);
    }

    /**
     * 修改部门并同步下级路径和受影响的登录信息。
     *
     * @param parameter 部门修改参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void updateDept(SysDeptUpdateDTO parameter) {
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        SysDept originalDepartment = this.getById(department.getId());
        if (originalDepartment == null) {
            throw new ServiceException("部门不存在或无权修改");
        }
        if (department.getId().equals(department.getParentId())) {
            throw new ServiceException("上级部门不能是当前部门");
        }

        boolean parentChanged = !originalDepartment.getParentId().equals(department.getParentId());
        department.setAncestors(parentChanged ? "0" : originalDepartment.getAncestors());
        if (parentChanged && department.getParentId() != 0L) {
            SysDept parent = this.getById(department.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在或超出修改权限");
            }
            // 新上级不能位于当前分支内，否则会形成循环。
            if (Arrays.asList(ValueParserUtils.splitToLongArray(parent.getAncestors(), ","))
                    .contains(department.getId())) {
                throw new ServiceException("不能将部门移动到自己的下级部门");
            }
            department.setAncestors(parent.getAncestors() + "," + parent.getId());
        }

        if (department.getLeaderUserId() != null && !userService.lambdaQuery()
                .eq(SysUser::getId, department.getLeaderUserId())
                .eq(SysUser::getDeptId, department.getId())
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode()).exists()) {
            throw new ServiceException("负责人必须是主部门为当前部门的正常用户");
        }
        // “本人”范围按负责人判断，不能借修改负责人绕过归属限制。
        DataPermissionContext.checkOwnership("sys_dept", department.getLeaderUserId(), department.getId());

        String originalPath = originalDepartment.getAncestors() + "," + department.getId();
        Set<Long> affectedDepartmentIds = new HashSet<>(Set.of(department.getId()));
        if (parentChanged || !originalDepartment.getStatus().equals(department.getStatus())) {
            // 移动或启停影响整个分支；完整下级ID同时用于路径更新校验和会话失效。
            affectedDepartmentIds.addAll(DataPermissionContext.getMetadata(() -> this.lambdaQuery()
                    .select(SysDept::getId)
                    .and(query -> query.eq(SysDept::getAncestors, originalPath)
                            .or().likeRight(SysDept::getAncestors, originalPath + ","))
                    .list()).stream().map(SysDept::getId).toList());
        }

        if (!this.updateById(department)) {
            throw new ServiceException("修改部门失败");
        }
        // 下级写入仍受权限过滤，未能更新完整分支时回滚整个修改。
        if (parentChanged && affectedDepartmentIds.size() > 1
                && baseMapper.updateDeptDescendantAncestors(department, originalPath) != affectedDepartmentIds.size() - 1) {
            throw new ServiceException("下级部门路径更新失败");
        }
        invalidateDeptSessions(originalDepartment, department, affectedDepartmentIds);
    }

    /**
     * 删除无业务关联的部门，提交后使受影响的授权会话失效。
     *
     * @param id 部门ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:delete", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void deleteDept(Long id) {
        SysDept department = this.getById(id);
        if (department == null) {
            throw new ServiceException("部门不存在或无权删除");
        }
        // 不可见的关联也会阻止删除，完整性查询必须覆盖全部数据。
        if (DataPermissionContext.getMetadata(() -> this.lambdaQuery().eq(SysDept::getParentId, id).exists())) {
            throw new ServiceException("部门存在下级部门，请先处理下级部门");
        }
        if (DataPermissionContext.getMetadata(() -> userService.lambdaQuery().eq(SysUser::getDeptId, id).exists())) {
            throw new ServiceException("部门仍有用户，请先调整用户归属");
        }
        if (DataPermissionContext.getMetadata(() -> postService.lambdaQuery().eq(SysPost::getDeptId, id).exists())) {
            throw new ServiceException("部门仍有岗位，请先处理关联岗位");
        }
        if (baseMapper.getRoleReferenceExistsByDeptId(id)) {
            throw new ServiceException("部门仍被角色数据范围引用，请先调整角色授权");
        }
        if (!this.removeById(id)) {
            throw new ServiceException("删除部门失败");
        }
        invalidateAncestorSessions(department);
    }

    /**
     * 根据部门修改内容，提交后使受影响的用户会话失效。
     *
     * @param originalDepartment 修改前的部门
     * @param department 修改后的部门
     * @param affectedDepartmentIds 当前部门及层级或状态变化影响的下级部门ID
     */
    private void invalidateDeptSessions(SysDept originalDepartment, SysDept department, Set<Long> affectedDepartmentIds) {
        boolean scopeChanged = !originalDepartment.getParentId().equals(department.getParentId())
                || !originalDepartment.getStatus().equals(department.getStatus());
        // 联系方式、编码、排序和备注不影响登录信息。
        if (!scopeChanged && originalDepartment.getDeptName().equals(department.getDeptName())
                && Objects.equals(originalDepartment.getLeaderUserId(), department.getLeaderUserId())) {
            return;
        }

        Set<Long> affectedUserIds;
        if (scopeChanged) {
            // 范围变化影响分支内的主部门用户、岗位成员和角色关联用户。
            affectedUserIds = userService.getAffectedUserIdsByDepartments(affectedDepartmentIds);
            // 移动或启停也会改变祖先的下级范围，需合并新旧祖先中受影响的用户。
            Set<Long> ancestorIds = new HashSet<>(Arrays.asList(ValueParserUtils.splitToLongArray(
                    originalDepartment.getAncestors() + "," + department.getAncestors(), ",")));
            ancestorIds.remove(0L);
            affectedUserIds.addAll(userService.getUserIdsByDepartmentAncestors(ancestorIds));
        } else {
            // 名称或负责人变化只影响本部门用户，会话失效查询不受操作者的可见范围限制。
            affectedUserIds = DataPermissionContext.getMetadata(() -> userService.lambdaQuery()
                    .select(SysUser::getId)
                    .eq(SysUser::getDeptId, department.getId())
                    .list()).stream().map(SysUser::getId).collect(Collectors.toSet());
        }
        // 事务提交后失效授权快照，回滚时保留会话。
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
    }

    /**
     * 新增或删除有效部门后，使祖先部门中受影响的用户会话在提交后失效。
     *
     * @param department 新增或删除的部门
     */
    private void invalidateAncestorSessions(SysDept department) {
        if (department.getParentId() == 0L || department.getStatus() != 0) {
            return;
        }
        Set<Long> ancestorIds = new HashSet<>(Arrays.asList(
                ValueParserUtils.splitToLongArray(department.getAncestors(), ",")));
        ancestorIds.remove(0L);
        // 整条祖先链启用时才影响下级授权范围，检查不受操作者可见范围限制。
        long enabledAncestorCount = DataPermissionContext.getMetadata(() -> this.lambdaQuery()
                .in(SysDept::getId, ancestorIds).eq(SysDept::getStatus, 0).count());
        if (enabledAncestorCount == ancestorIds.size()) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                    userService.getUserIdsByDepartmentAncestors(ancestorIds));
        }
    }
}
