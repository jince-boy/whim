package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.entity.SysDept;
import com.whim.system.model.vo.dept.SysDeptVO;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门管理服务。
 */
public interface ISysDeptService extends IService<SysDept> {
    /**
     * 查询当前用户数据权限范围内的部门树，不分页。
     *
     * @return 按排序值和ID升序排列的部门树
     */
    List<SysDeptVO> getDeptList();

    /**
     * 新增部门并维护受影响的登录授权范围，不按操作者的部门数据范围限制新增位置。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    Long addDept(SysDeptAddDTO parameter);
}
