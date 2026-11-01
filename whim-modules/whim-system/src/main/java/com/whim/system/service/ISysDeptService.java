package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.dept.SysDeptQueryDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptSaveDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.mybatisplus.model.vo.PageDataVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 部门树及部门维护服务。
 */
public interface ISysDeptService extends IService<SysDept> {
    /**
     * 分页查询主部门为当前部门的正常用户，供选择负责人。
     *
     * @param id 已创建的部门ID
     * @param query 用户筛选和分页参数
     * @return 本部门负责人候选用户
     */
    PageDataVO<SysDeptLeaderVO> getDeptLeaderPage(Long id, SysDeptLeaderQueryDTO query);

    /**
     * 根据ID查询当前操作可见的部门详情。
     *
     * @param id 部门ID
     * @return 部门详情
     */
    SysDeptVO getDeptDetail(Long id);

    /**
     * 查询部门树，筛选时保留可见祖先，无权访问的上级不返回。
     *
     * @param query 查询参数
     * @return 按排序值和ID升序排列的部门树
     */
    List<SysDeptVO> getDeptList(SysDeptQueryDTO query);

    /**
     * 在授权的上级部门范围内新增部门，创建后再设置负责人。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    Long addDept(SysDeptSaveDTO parameter);

    /**
     * 完整修改部门，移动时同步维护全部下级的祖先路径。
     *
     * @param parameter 部门修改参数
     */
    void updateDept(SysDeptUpdateDTO parameter);

    /**
     * 修改部门状态，停用后整条下级分支退出有效授权范围。
     *
     * @param parameter 包含部门ID和状态的修改参数
     */
    void updateDeptStatus(SysOrganizationStatusDTO parameter);

    /**
     * 删除没有下级及业务引用的部门。
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);
}
