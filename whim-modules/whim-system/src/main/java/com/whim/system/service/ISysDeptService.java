package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.dto.dept.DeptCreateDTO;
import com.whim.system.model.dto.dept.DeptUpdateDTO;
import com.whim.system.model.vo.dept.DeptVO;

import java.util.List;
import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统部门表服务接口
 */
public interface ISysDeptService extends IService<SysDept> {
    /** 查询当前租户部门节点。 */
    List<DeptVO> listCurrentTenantDepartments();

    /** 查询当前租户有效部门，不允许跨租户引用。 */
    SysDept getRequiredActiveDepartment(Long deptId, Long tenantId);

    /** 查询有效部门及其有效子部门ID。 */
    Set<Long> getActiveDescendantIds(Long deptId, Long tenantId);

    /** 创建当前租户部门。 */
    Long createCurrentTenantDepartment(DeptCreateDTO request);

    /** 修改当前租户部门基础信息。 */
    void updateCurrentTenantDepartment(Long deptId, DeptUpdateDTO request);

    /** 修改当前租户部门状态。 */
    void setCurrentTenantDepartmentStatus(Long deptId, Integer status);
}

