package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.dto.dept.DeptCreateDTO;
import com.whim.system.model.dto.dept.DeptUpdateDTO;
import com.whim.system.model.vo.dept.DeptVO;

import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description SysDept业务服务。
 */
public interface ISysDeptService extends IService<SysDept> {
    /** 查询本次操作可见的部门节点。 */
    List<DeptVO> listDepartments();
    /** 查询有效部门并验证完整祖先链。 */
    SysDept getRequiredActiveDepartment(Long deptId);
    /** 查询有效部门及其有效下级部门。 */
    Set<Long> getActiveDescendantIds(Long deptId);
    /** 创建部门并验证父部门范围。 */
    Long createDepartment(DeptCreateDTO request);
    /** 修改部门名称和排序，不隐式迁移部门树。 */
    void updateDepartment(Long deptId, DeptUpdateDTO request);
    /** 启用或停用无引用部门。 */
    void setDepartmentStatus(Long deptId, Integer status);
    /** 删除没有子部门、用户、岗位或授权引用的部门。 */
    void deleteDepartment(Long deptId);
}

