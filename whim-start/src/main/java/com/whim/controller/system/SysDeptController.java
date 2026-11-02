package com.whim.controller.system;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.dept.SysDeptAddDTO;
import com.whim.system.model.vo.dept.SysDeptVO;
import com.whim.system.service.ISysDeptService;
import com.whim.web.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 在当前用户授权的上级部门下新增部门。
     *
     * @param parameter 部门新增参数
     * @return 新增部门ID
     */
    @PostMapping
    @SystemCheckPermission("system:dept:create")
    public Result<Long> addDept(@Valid @RequestBody SysDeptAddDTO parameter) {
        return Result.success("新增部门成功", deptService.addDept(parameter));
    }
}
