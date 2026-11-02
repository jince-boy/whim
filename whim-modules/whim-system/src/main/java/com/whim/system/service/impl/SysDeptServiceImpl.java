package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.core.utils.ValueParserUtils;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.system.mapper.SysDeptMapper;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
     * 新增部门并校验组织层级，提交后失效因新增下级而变化的登录授权范围。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDept(SysDeptAddDTO parameter) {
        SysDept department = BeanConvertUtils.convert(parameter, SysDept.class);
        Set<Long> ancestorIds = new HashSet<>();
        if (department.getParentId() == 0L) {
            department.setAncestors("0");
        } else {
            SysDept parent = this.getById(department.getParentId());
            if (parent == null) {
                throw new ServiceException("上级部门不存在");
            }
            String ancestors = parent.getAncestors() + "," + parent.getId();
            department.setAncestors(ancestors);
            ancestorIds.addAll(Arrays.asList(ValueParserUtils.splitToLongArray(ancestors, ",")));
            ancestorIds.remove(0L);
            if (department.getStatus() == 0) {
                long enabledAncestorCount = this.lambdaQuery()
                        .in(SysDept::getId, ancestorIds)
                        .eq(SysDept::getStatus, 0)
                        .count();
                if (enabledAncestorCount != ancestorIds.size()) {
                    throw new ServiceException("正常部门的上级及祖先部门必须全部启用");
                }
            }
        }
        if (!this.save(department)) {
            throw new ServiceException("新增部门失败");
        }
        if (department.getStatus() == 0 && !ancestorIds.isEmpty()) {
            authenticationSession.kickoutAfterCommit(AuthUserType.SYSTEM,
                    baseMapper.getUserIdsByDepartmentAncestors(ancestorIds));
        }
        return department.getId();
    }
}
