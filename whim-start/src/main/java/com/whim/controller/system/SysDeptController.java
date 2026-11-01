package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dept.SysDeptQueryDTO;
import com.whim.system.model.dto.dept.SysDeptLeaderQueryDTO;
import com.whim.system.model.dto.dept.SysDeptSaveDTO;
import com.whim.system.model.dto.dept.SysDeptUpdateDTO;
import com.whim.system.model.dto.organization.SysOrganizationStatusDTO;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.model.vo.dept.SysDeptLeaderVO;
import com.whim.mybatisplus.model.vo.PageDataVO;
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
 * @date 2026/11/01
 * @description 系统部门树及部门管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/dept")
public class SysDeptController {
    private final ISysDeptService deptService;

    /**
     * 分页查询本部门正常用户，供编辑部门时选择负责人。
     *
     * @param id 已创建的部门ID
     * @param query 用户筛选和分页参数
     * @return 部门负责人候选用户
     */
    @GetMapping("/{id}/leaderOptions")
    @SystemCheckPermission("system:dept:update")
    public Result<PageDataVO<SysDeptLeaderVO>> getDeptLeaderPage(@PathVariable Long id,
                                                               @Valid SysDeptLeaderQueryDTO query) {
        return Result.success("部门负责人候选用户查询成功", deptService.getDeptLeaderPage(id, query));
    }

    /**
     * 查询部门管理树，也可排除当前分支用于上级选择。
     *
     * @param query 查询参数
     * @return 当前权限范围内的部门树
     */
    @GetMapping
    @SystemCheckPermission("system:dept:list")
    public Result<List<SysDeptVO>> getDeptList(@Valid SysDeptQueryDTO query) {
        return Result.success("部门树查询成功", deptService.getDeptList(query));
    }

    /**
     * 查询部门详情，复用部门查询权限。
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    @SystemCheckPermission("system:dept:list")
    public Result<SysDeptVO> getDeptDetail(@PathVariable Long id) {
        return Result.success("部门详情查询成功", deptService.getDeptDetail(id));
    }

    /**
     * 新增部门。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    @PostMapping
    @SystemCheckPermission("system:dept:create")
    public Result<Long> addDept(@Valid @RequestBody SysDeptSaveDTO parameter) {
        return Result.success("部门新增成功", deptService.addDept(parameter));
    }

    /**
     * 完整修改部门，移动部门时同步更新下级路径。
     *
     * @param parameter 部门修改参数
     * @return 修改结果
     */
    @PutMapping
    @SystemCheckPermission("system:dept:update")
    public Result<Void> updateDept(@Valid @RequestBody SysDeptUpdateDTO parameter) {
        deptService.updateDept(parameter);
        return Result.success("部门修改成功");
    }

    /**
     * 使用独立权限修改部门状态。
     *
     * @param parameter 包含部门ID和状态的修改参数
     * @return 状态修改结果
     */
    @PutMapping("/status")
    @SystemCheckPermission("system:dept:status")
    public Result<Void> updateDeptStatus(@Valid @RequestBody SysOrganizationStatusDTO parameter) {
        deptService.updateDeptStatus(parameter);
        return Result.success("部门状态修改成功");
    }

    /**
     * 删除没有下级及业务引用的部门。
     *
     * @param id 部门ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SystemCheckPermission("system:dept:delete")
    public Result<Void> deleteDept(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success("部门删除成功");
    }
}
