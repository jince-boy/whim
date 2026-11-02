package com.whim.controller.system;

import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.vo.dept.SysDeptDetailVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.service.ISysDeptService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 系统部门管理控制器。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/dept")
public class SysDeptController {
    private final ISysDeptService deptService;

    /**
     * 分页查询当前部门的负责人候选用户。
     *
     * @param id 部门ID
     * @param query 分页及搜索参数
     * @return 本部门的正常用户
     */
    @GetMapping("/{id}/leaderOptions")
    @SystemCheckPermission("system:dept:update")
    public Result<PageDataVO<SysDeptLeaderVO>> getDeptLeaderOptions(@PathVariable Long id,
                                                                  @Valid SysDeptLeaderQueryDTO query) {
        return Result.success("负责人候选查询成功", deptService.getDeptLeaderOptions(id, query));
    }

    /**
     * 查询修改表单所需的部门详情。
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    @SystemCheckPermission("system:dept:update")
    public Result<SysDeptDetailVO> getDeptDetail(@PathVariable Long id) {
        return Result.success("部门详情查询成功", deptService.getDeptDetail(id));
    }

    /**
     * 查询当前用户数据权限范围内的部门树，不分页。
     *
     * @return 可见的部门树
     */
    @GetMapping
    @SystemCheckPermission("system:dept:list")
    public Result<List<SysDeptVO>> getDeptList() {
        return Result.success("部门列表查询成功", deptService.getDeptList());
    }

    /**
     * 校验部门新增功能权限后创建部门，不限制操作者的部门归属和数据范围。
     *
     * @param parameter 部门新增参数
     * @return 新增部门成功响应
     */
    @PostMapping
    @SystemCheckPermission("system:dept:create")
    public Result<Void> addDept(@Valid @RequestBody SysDeptAddDTO parameter) {
        deptService.addDept(parameter);
        return Result.success("新增部门成功");
    }

    /**
     * 修改部门及其组织归属。
     *
     * @param parameter 部门修改参数
     * @return 修改部门成功响应
     */
    @PutMapping
    @SystemCheckPermission("system:dept:update")
    public Result<Void> updateDept(@Valid @RequestBody SysDeptUpdateDTO parameter) {
        deptService.updateDept(parameter);
        return Result.success("修改部门成功");
    }

    /**
     * 删除没有业务关联的部门。
     *
     * @param id 部门ID
     * @return 删除部门成功响应
     */
    @DeleteMapping("/{id}")
    @SystemCheckPermission("system:dept:delete")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success("删除部门成功");
    }
}
