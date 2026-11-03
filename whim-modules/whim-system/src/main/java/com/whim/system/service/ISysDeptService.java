package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptQueryDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.vo.dept.SysDeptDetailVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.system.model.vo.dept.SysDeptVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门管理服务。
 */
public interface ISysDeptService extends IService<SysDept> {
    /**
     * 分页查询可见部门内可作为负责人的正常用户。
     *
     * @param query 部门ID、分页及搜索参数
     * @return 主部门为指定部门的负责人候选用户
     */
    PageDataVO<SysDeptLeaderVO> getDeptLeaderOptions(SysDeptLeaderQueryDTO query);

    /**
     * 查询修改权限范围内的部门详情。
     *
     * @param id 部门ID
     * @return 部门详情及当前有效负责人信息
     */
    SysDeptDetailVO getDeptDetail(Long id);

    /**
     * 按名称和状态查询可见部门树，不分页。
     *
     * @param query 部门名称和状态查询条件
     * @return 按排序值和ID升序排列的部门树
     */
    List<SysDeptVO> getDeptList(SysDeptQueryDTO query);

    /**
     * 新增部门并维护受影响的登录授权范围，不按操作者的部门数据范围限制新增位置。
     * 允许在停用的上级或祖先下新增部门，实际授权仍取决于整条部门链的启用状态。
     *
     * @param parameter 部门新增参数
     */
    void addDept(SysDeptAddDTO parameter);

    /**
     * 修改部门并维护下级路径，提交后使受影响用户的登录会话失效。
     *
     * @param parameter 部门修改参数
     */
    void updateDept(SysDeptUpdateDTO parameter);

    /**
     * 逻辑删除没有下级部门、用户、岗位或角色数据范围引用的部门。
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);
}
