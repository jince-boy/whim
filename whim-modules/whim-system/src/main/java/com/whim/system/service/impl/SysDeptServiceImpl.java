package com.whim.system.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.DataAccessDeniedException;
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
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.entity.SysUser;
import com.whim.system.model.enums.SysUserStatus;
import com.whim.system.model.vo.dept.SysDeptDetailVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.service.ISysDeptService;
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

    /**
     * 分页查询可作为当前部门负责人的正常用户。
     *
     * @param id 部门ID
     * @param query 分页及搜索参数
     * @return 本部门的负责人候选用户
     */
    @Override
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public PageDataVO<SysDeptLeaderVO> getDeptLeaderOptions(Long id, SysDeptLeaderQueryDTO query) {
        if (this.getById(id) == null) {
            throw new ServiceException("部门不存在或无权修改");
        }
        String keyword = query.getKeyword();
        Page<SysUser> users = userService.lambdaQuery()
                .select(SysUser::getId, SysUser::getName, SysUser::getUsername)
                .eq(SysUser::getDeptId, id)
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysUser::getName, keyword).or().like(SysUser::getUsername, keyword))
                .orderByAsc(SysUser::getId)
                .page(query.getPage());
        return new PageDataVO<>(users.convert(user -> BeanConvertUtils.convert(user, SysDeptLeaderVO.class)));
    }

    /**
     * 查询可修改的部门详情，并回填仍符合条件的负责人姓名和用户名。
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
            SysUser leader = userService.lambdaQuery()
                    .select(SysUser::getName, SysUser::getUsername)
                    .eq(SysUser::getId, department.getLeaderUserId())
                    .eq(SysUser::getDeptId, id)
                    .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode())
                    .one();
            if (leader != null) {
                detail.setLeaderName(leader.getName());
                detail.setLeaderUsername(leader.getUsername());
            }
        }
        return detail;
    }

    /**
     * 查询授权范围内的部门并组装树，上级不可见的部门作为返回树的根节点。
     *
     * @return 按排序值和ID升序排列的部门树
     */
    @Override
    @DataPermission(permission = "system:dept:list", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public List<SysDeptVO> getDeptList() {
        List<SysDeptVO> departments = BeanConvertUtils.convertList(
                this.lambdaQuery().orderByAsc(SysDept::getSort, SysDept::getId).list(), SysDeptVO.class);
        Map<Long, SysDeptVO> departmentMap = departments.stream()
                .collect(Collectors.toMap(SysDeptVO::getId, department -> department));
        List<SysDeptVO> roots = new ArrayList<>();
        for (SysDeptVO department : departments) {
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
     * 新增部门并维护组织层级，允许上级停用，提交后失效受影响的登录授权范围。
     *
     * @param parameter 部门新增参数
     */
    @Override
    @Lock4j(name = "system:dept")
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
        if (department.getParentId() == 0L || department.getStatus() != 0) {
            return;
        }

        // 祖先全部启用时，新增部门才会扩大现有登录用户的数据范围。
        Set<Long> ancestorIds = new HashSet<>(Arrays.asList(
                ValueParserUtils.splitToLongArray(department.getAncestors(), ",")));
        ancestorIds.remove(0L);
        long enabledAncestorCount = this.lambdaQuery()
                .in(SysDept::getId, ancestorIds)
                .eq(SysDept::getStatus, 0)
                .count();
        if (enabledAncestorCount == ancestorIds.size()) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                    baseMapper.getUserIdsByDepartmentAncestors(ancestorIds));
        }
    }

    /**
     * 修改部门并维护下级路径，提交后使受影响用户的登录会话失效。
     *
     * @param parameter 部门修改参数
     */
    @Override
    @Lock4j(name = "system:dept")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:update", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void updateDept(SysDeptUpdateDTO parameter) {
        SysDept previous = this.getById(parameter.getId());
        if (previous == null) {
            throw new ServiceException("部门不存在或无权修改");
        }
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        if (department.getId().equals(department.getParentId())) {
            throw new ServiceException("上级部门不能是当前部门");
        }
        boolean parentChanged = !previous.getParentId().equals(department.getParentId());
        department.setAncestors(previous.getAncestors());
        if (parentChanged) {
            department.setAncestors("0");
            if (department.getParentId() == 0L) {
                DataPermissionContext.checkOwnership("sys_dept", null, 0L);
            } else {
                SysDept parent = this.getById(department.getParentId());
                if (parent == null) {
                    throw new ServiceException("上级部门不存在或超出修改权限");
                }
                if (Arrays.asList(ValueParserUtils.splitToLongArray(parent.getAncestors(), ","))
                        .contains(department.getId())) {
                    throw new ServiceException("不能将部门移动到自己的下级部门");
                }
                department.setAncestors(parent.getAncestors() + "," + parent.getId());
            }
        }
        if (department.getLeaderUserId() != null && !userService.lambdaQuery()
                .eq(SysUser::getId, department.getLeaderUserId())
                .eq(SysUser::getDeptId, department.getId())
                .eq(SysUser::getStatus, SysUserStatus.ENABLED.getCode()).exists()) {
            throw new ServiceException("负责人必须是主部门为当前部门的正常用户");
        }
        boolean leaderChanged = !Objects.equals(previous.getLeaderUserId(), department.getLeaderUserId());
        if (parentChanged || leaderChanged) {
            DataPermissionContext.checkOwnership("sys_dept", department.getLeaderUserId(), department.getId());
        }

        boolean statusChanged = !previous.getStatus().equals(department.getStatus());
        if (statusChanged && department.getStatus() == 0 && department.getParentId() != 0L) {
            Set<Long> ancestorIds = new HashSet<>(Arrays.asList(
                    ValueParserUtils.splitToLongArray(department.getAncestors(), ",")));
            ancestorIds.remove(0L);
            if (baseMapper.getEnabledDeptCountByIds(ancestorIds) != ancestorIds.size()) {
                throw new ServiceException("启用部门前，请先启用全部上级部门");
            }
        }
        boolean subtreeChanged = parentChanged || statusChanged;
        Set<Long> departmentIds = new HashSet<>();
        departmentIds.add(department.getId());
        if (subtreeChanged) {
            departmentIds.addAll(baseMapper.getDescendantDeptIds(department.getId()));
            if (this.lambdaQuery().in(SysDept::getId, departmentIds).count() != departmentIds.size()) {
                throw new DataAccessDeniedException("下级部门超出修改权限，无法调整上级或启停状态");
            }
        }

        if (!this.updateById(department)) {
            throw new ServiceException("修改部门失败");
        }
        if (parentChanged && departmentIds.size() > 1) {
            String previousPath = previous.getAncestors() + "," + previous.getId();
            if (baseMapper.updateDeptDescendantAncestors(department, previousPath) != departmentIds.size() - 1) {
                throw new ServiceException("下级部门路径更新失败");
            }
        }

        // 电话、邮箱、排序、编码和备注不影响登录信息，保存后即可结束。
        if (!subtreeChanged && !leaderChanged && previous.getDeptName().equals(department.getDeptName())) {
            return;
        }
        Set<Long> affectedUserIds = baseMapper.getUserIdsByDepartments(departmentIds);
        if (subtreeChanged) {
            Set<Long> ancestorIds = new HashSet<>(Arrays.asList(ValueParserUtils.splitToLongArray(
                    previous.getAncestors() + "," + department.getAncestors(), ",")));
            ancestorIds.remove(0L);
            if (!ancestorIds.isEmpty()) {
                affectedUserIds.addAll(baseMapper.getUserIdsByDepartmentAncestors(ancestorIds));
            }
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
    }

    /**
     * 逻辑删除无业务关联的部门，提交后使上级部门受影响的登录会话失效。
     *
     * @param id 部门ID
     */
    @Override
    @Lock4j(name = "system:dept")
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(permission = "system:dept:delete", tables =
            @DataPermissionTable(name = "sys_dept", departmentColumn = "id", userColumn = "leader_user_id"))
    public void deleteDept(Long id) {
        SysDept department = this.getById(id);
        if (department == null) {
            throw new ServiceException("部门不存在或无权删除");
        }
        String blockReason = baseMapper.getDeptDeleteBlockReason(id);
        if (blockReason != null) {
            throw new ServiceException(blockReason);
        }
        Set<Long> affectedUserIds = new HashSet<>();
        if (department.getStatus() == 0 && department.getParentId() != 0L) {
            Set<Long> ancestorIds = new HashSet<>(Arrays.asList(
                    ValueParserUtils.splitToLongArray(department.getAncestors(), ",")));
            ancestorIds.remove(0L);
            if (baseMapper.getEnabledDeptCountByIds(ancestorIds) == ancestorIds.size()) {
                affectedUserIds.addAll(baseMapper.getUserIdsByDepartmentAncestors(ancestorIds));
            }
        }
        if (!this.removeById(id)) {
            throw new ServiceException("删除部门失败");
        }
        authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM, affectedUserIds);
    }
}
