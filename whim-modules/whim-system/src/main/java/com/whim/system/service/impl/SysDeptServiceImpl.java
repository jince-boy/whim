package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.ValueParserUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.permission.DataPermissionContext;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.dto.dept.SysDeptQueryDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptSaveDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysPost;
import com.whim.system.model.entity.SysRoleDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.system.service.ISysDeptService;
import com.whim.system.service.ISysPostService;
import com.whim.system.service.ISysRoleDeptService;
import com.whim.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门层级维护、操作级数据权限及登录快照失效处理。
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {
    private final ISysUserService userService;
    private final ISysPostService postService;
    private final ISysRoleDeptService roleDeptService;
    private final AuthenticationSession authenticationSession;

    /**
     * 校验部门修改权限后，分页读取本部门正常用户的负责人选项。
     *
     * @param id 已创建的部门ID
     * @param query 用户筛选和分页参数
     * @return 本部门负责人候选用户
     */
    @Override
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public PageDataVO<SysDeptLeaderVO> getDeptLeaderPage(Long id, SysDeptLeaderQueryDTO query) {
        getRequiredDept(id);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getUsername, SysUser::getName)
                .eq(SysUser::getDeptId, id).eq(SysUser::getStatus, 0);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getName, keyword));
        }
        wrapper.orderByAsc(SysUser::getId);
        return new PageDataVO<>(userService.page(query.getPage(), wrapper)
                .convert(user -> BeanConvertUtils.convert(user, SysDeptLeaderVO.class)));
    }

    /**
     * 根据ID查询当前操作可见的部门详情。
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @Override
    @DataPermission(permission = "system:dept:list", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public SysDeptVO getDeptDetail(Long id) {
        SysDeptVO department = BeanConvertUtils.convert(getRequiredDept(id), SysDeptVO.class);
        fillDeptLeaderNames(List.of(department));
        return department;
    }

    /**
     * 查询部门树，筛选时只补充当前操作可见的祖先。
     *
     * @param query 查询参数
     * @return 按排序值和ID升序排列的部门树
     */
    @Override
    @DataPermission(permission = "system:dept:list", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public List<SysDeptVO> getDeptList(SysDeptQueryDTO query) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort, SysDept::getId);
        if (query.getExcludeDeptId() != null) {
            wrapper.ne(SysDept::getId, query.getExcludeDeptId())
                    .apply("FIND_IN_SET({0}, ancestors) = 0", query.getExcludeDeptId());
        }
        List<SysDeptVO> departments = BeanConvertUtils.convertList(this.list(wrapper), SysDeptVO.class);
        fillDeptLeaderNames(departments);
        Map<Long, SysDeptVO> departmentMap = new LinkedHashMap<>();
        departments.forEach(department -> departmentMap.put(department.getId(), department));
        Set<Long> retainedIds = new LinkedHashSet<>();
        for (SysDeptVO department : departments) {
            if ((StringUtils.isBlank(query.getDeptName())
                    || Strings.CI.contains(department.getDeptName(), query.getDeptName().trim()))
                    && (query.getStatus() == null || query.getStatus().equals(department.getStatus()))) {
                SysDeptVO current = department;
                while (current != null && retainedIds.add(current.getId())) {
                    current = departmentMap.get(current.getParentId());
                }
            }
        }
        List<SysDeptVO> roots = new ArrayList<>();
        for (SysDeptVO department : departments) {
            if (!retainedIds.contains(department.getId())) {
                continue;
            }
            SysDeptVO parent = departmentMap.get(department.getParentId());
            if (parent == null) {
                roots.add(department);
            } else {
                parent.getChildren().add(department);
            }
        }
        return roots;
    }

    /**
     * 在授权的上级部门范围内新增部门，负责人在用户归属后设置。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:create", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "parent_id", userColumn = "leader_user_id"))
    public Long addDept(SysDeptSaveDTO parameter) {
        if (parameter.getLeaderUserId() != null) {
            throw new ServiceException("请先创建部门并分配用户，再从本部门用户中选择负责人");
        }
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        department.setDeptName(department.getDeptName().trim());
        department.setDeptCode(department.getDeptCode().trim());
        validateDeptParent(department);
        // 新部门尚未进入登录快照，新增范围按挂载的上级部门校验。
        DataPermissionContext.checkOwnership("sys_dept", null, department.getParentId());
        if (!this.save(department)) {
            throw new ServiceException("新增部门失败");
        }
        invalidateDepartmentSessions(List.of(department), department.getAncestors());
        return department.getId();
    }

    /**
     * 修改部门，负责人变更时校验新归属，移动时核验完整分支并更新下级路径。
     *
     * @param parameter 部门修改参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void updateDept(SysDeptUpdateDTO parameter) {
        Long id = parameter.getId();
        SysDept previous = getRequiredDept(id);
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        department.setDeptName(department.getDeptName().trim());
        department.setDeptCode(department.getDeptCode().trim());
        validateDeptParent(department);
        validateDeptLeader(id, department.getLeaderUserId());
        if (!Objects.equals(previous.getLeaderUserId(), department.getLeaderUserId())) {
            DataPermissionContext.checkOwnership("sys_dept", department.getLeaderUserId(), id);
        }
        boolean moved = !previous.getParentId().equals(department.getParentId());
        boolean statusChanged = !previous.getStatus().equals(department.getStatus());
        List<SysDept> branch = moved || statusChanged ? baseMapper.getDepartmentBranch(id) : List.of(previous);
        if (moved || statusChanged) {
            for (SysDept descendant : branch) {
                DataPermissionContext.checkOwnership("sys_dept", descendant.getLeaderUserId(), descendant.getId());
            }
        }
        if (moved) {
            DataPermissionContext.checkOwnership("sys_dept", department.getLeaderUserId(), department.getParentId());
            updateDeptDescendantPaths(previous, department, branch);
        }
        if (!this.updateById(department)) {
            throw new ServiceException("修改部门失败");
        }
        invalidateDepartmentSessions(branch, previous.getAncestors(), department.getAncestors());
    }

    /**
     * 修改部门状态，停用分支前核验全部受影响部门的操作权限。
     *
     * @param parameter 包含部门ID和状态的修改参数
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:status", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void updateDeptStatus(SysOrganizationStatusDTO parameter) {
        Long id = parameter.getId();
        Integer status = parameter.getStatus();
        SysDept department = getRequiredDept(id);
        if (department.getStatus().equals(status)) {
            return;
        }
        List<SysDept> branch = baseMapper.getDepartmentBranch(id);
        for (SysDept descendant : branch) {
            DataPermissionContext.checkOwnership("sys_dept", descendant.getLeaderUserId(), descendant.getId());
        }
        department.setStatus(status);
        if (status == 0) {
            validateDeptParent(department);
        }
        if (!this.updateById(department)) {
            throw new ServiceException("修改部门状态失败");
        }
        invalidateDepartmentSessions(branch, department.getAncestors());
    }

    /**
     * 删除没有下级、用户、岗位及角色自定义范围引用的部门。
     *
     * @param id 部门ID
     */
    @Override
    @Lock4j(name = "system:organization")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:delete", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void deleteDept(Long id) {
        SysDept department = getRequiredDept(id);
        if (baseMapper.getDepartmentBranch(id).size() > 1) {
            throw new ServiceException("部门仍有下级部门，不能删除");
        }
        if (userService.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id))) {
            throw new ServiceException("部门仍有用户，不能删除");
        }
        if (postService.exists(new LambdaQueryWrapper<SysPost>().eq(SysPost::getDeptId, id))) {
            throw new ServiceException("部门仍有岗位，不能删除");
        }
        if (roleDeptService.exists(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getDeptId, id))) {
            throw new ServiceException("部门仍被角色自定义数据范围引用，不能删除");
        }
        if (!this.removeById(id)) {
            throw new ServiceException("删除部门失败");
        }
        invalidateDepartmentSessions(List.of(department), department.getAncestors());
    }

    /**
     * 获取本次操作有权访问的部门，隐藏不存在和越权的差异。
     *
     * @param id 部门ID
     * @return 部门实体
     */
    private SysDept getRequiredDept(Long id) {
        SysDept department = this.getById(id);
        if (department == null) {
            throw new ServiceException("部门不存在或无权访问");
        }
        return department;
    }

    /**
     * 校验上级和祖先链并计算部门路径，禁止自身或下级作为上级。
     *
     * @param department 待保存的部门
     */
    private void validateDeptParent(SysDept department) {
        if (department.getParentId() == 0L) {
            department.setAncestors("0");
            return;
        }
        SysDept parent = baseMapper.getDepartmentReferenceById(department.getParentId());
        if (parent == null) {
            throw new ServiceException("上级部门不存在");
        }
        if (Objects.equals(department.getId(), parent.getId())
                || Arrays.asList(ValueParserUtils.splitToLongArray(parent.getAncestors(), ","))
                .contains(department.getId())) {
            throw new ServiceException("不能将自身或下级部门设置为上级");
        }
        if (department.getStatus() == 0
                && baseMapper.getValidDepartmentsByIds(Set.of(parent.getId())).isEmpty()) {
            throw new ServiceException("正常部门的上级及祖先部门必须全部启用");
        }
        String ancestors = parent.getAncestors() + "," + parent.getId();
        if (ancestors.length() > 500) {
            throw new ServiceException("部门祖先路径不能超过500个字符");
        }
        department.setAncestors(ancestors);
    }

    /**
     * 部门移动后按新祖先前缀更新下级路径，保留下级的其他业务字段。
     *
     * @param previous 移动前的部门
     * @param department 移动后的部门
     * @param branch 包含当前部门的完整分支
     */
    private void updateDeptDescendantPaths(SysDept previous, SysDept department, List<SysDept> branch) {
        String previousPrefix = previous.getAncestors() + "," + previous.getId();
        String nextPrefix = department.getAncestors() + "," + department.getId();
        List<SysDept> descendants = new ArrayList<>();
        for (SysDept descendant : branch) {
            if (department.getId().equals(descendant.getId())) {
                continue;
            }
            String ancestors = nextPrefix + descendant.getAncestors().substring(previousPrefix.length());
            if (ancestors.length() > 500) {
                throw new ServiceException("移动后下级部门的祖先路径超过500个字符");
            }
            descendant.setAncestors(ancestors);
            descendants.add(descendant);
        }
        if (!descendants.isEmpty() && !this.updateBatchById(descendants)) {
            throw new ServiceException("更新下级部门路径失败");
        }
    }

    /**
     * 校验负责人为主部门属于当前部门的正常用户。
     *
     * @param deptId 当前部门ID
     * @param leaderUserId 负责人用户ID，null表示不设置
     */
    private void validateDeptLeader(Long deptId, Long leaderUserId) {
        if (leaderUserId != null && !userService.exists(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, leaderUserId).eq(SysUser::getDeptId, deptId).eq(SysUser::getStatus, 0))) {
            throw new ServiceException("部门负责人必须是主部门属于当前部门的正常用户");
        }
    }

    /**
     * 批量补充有效负责人的姓名与用户名，避免部门树逐行查询用户。
     *
     * @param departments 当前操作可见的部门列表
     */
    private void fillDeptLeaderNames(List<SysDeptVO> departments) {
        Set<Long> leaderIds = departments.stream().map(SysDeptVO::getLeaderUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (leaderIds.isEmpty()) {
            return;
        }
        Map<Long, SysUser> leaders = userService.list(new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId, SysUser::getName, SysUser::getUsername, SysUser::getDeptId)
                        .in(SysUser::getId, leaderIds).eq(SysUser::getStatus, 0))
                .stream().collect(Collectors.toMap(SysUser::getId, user -> user));
        for (SysDeptVO department : departments) {
            SysUser leader = leaders.get(department.getLeaderUserId());
            if (leader != null && department.getId().equals(leader.getDeptId())) {
                department.setLeaderName(leader.getName());
                department.setLeaderUsername(leader.getUsername());
            }
        }
    }

    /**
     * 在事务提交后失效部门变化影响的登录快照。
     *
     * @param branch 变化的部门分支
     * @param ancestorPaths 变化前后的祖先路径
     */
    private void invalidateDepartmentSessions(List<SysDept> branch, String... ancestorPaths) {
        Set<Long> departmentIds = branch.stream().map(SysDept::getId).collect(Collectors.toSet());
        Set<Long> ancestorIds = new LinkedHashSet<>();
        for (String ancestors : ancestorPaths) {
            ancestorIds.addAll(Arrays.asList(ValueParserUtils.splitToLongArray(ancestors, ",")));
        }
        ancestorIds.remove(0L);
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                baseMapper.getUserIdsByDepartmentChange(departmentIds, ancestorIds));
    }
}
