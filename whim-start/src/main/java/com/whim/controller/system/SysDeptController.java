package com.whim.controller.system;


import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dept.DeptCreateDTO;
import com.whim.system.model.dto.dept.DeptStatusDTO;
import com.whim.system.model.dto.dept.DeptUpdateDTO;
import com.whim.system.model.vo.dept.DeptVO;
import com.whim.system.service.ISysDeptService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * @date 2026/07/02
 * @description 系统部门表控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController {

    /**
     * 系统部门表服务对象
     */
    private final ISysDeptService sysDeptService;

    /** 查询当前租户部门节点。 */
    @GetMapping
    @SystemCheckPermission("system:dept:list")
    public Result<List<DeptVO>> listDepartments() {
        return Result.success("部门查询成功", sysDeptService.listCurrentTenantDepartments());
    }

    /** 创建当前租户部门。 */
    @PostMapping
    @SystemCheckPermission("system:dept:create")
    public Result<Long> createDepartment(@RequestBody @Valid DeptCreateDTO request) {
        return Result.success("部门创建成功", sysDeptService.createCurrentTenantDepartment(request));
    }

    /** 修改当前租户部门名称与排序。 */
    @PutMapping("/{deptId}")
    @SystemCheckPermission("system:dept:update")
    public Result<Void> updateDepartment(@PathVariable Long deptId, @RequestBody @Valid DeptUpdateDTO request) {
        sysDeptService.updateCurrentTenantDepartment(deptId, request);
        return Result.success("部门修改成功");
    }

    /** 启用或停用当前租户部门。 */
    @PutMapping("/{deptId}/status")
    @SystemCheckPermission("system:dept:status")
    public Result<Void> setDepartmentStatus(@PathVariable Long deptId, @RequestBody @Valid DeptStatusDTO request) {
        sysDeptService.setCurrentTenantDepartmentStatus(deptId, request.getStatus());
        return Result.success("部门状态修改成功");
    }
}

