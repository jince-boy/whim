package com.whim.controller.system;

import com.whim.core.web.Result;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysPermission.SysPermissionInsertDTO;
import com.whim.system.model.dto.sysPermission.SysPermissionQueryDTO;
import com.whim.system.model.dto.sysPermission.SysPermissionUpdateDTO;
import com.whim.system.model.vo.SysPermissionVO;
import com.whim.system.service.ISysPermissionService;
import com.whim.web.annotation.SystemApiPrefix;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jince
 * date: 2025/9/9 16:20
 * description: 菜单管理
 */
@SystemApiPrefix
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysPermissionController {
    private final ISysPermissionService sysPermissionService;


    /**
     * 获取菜单列表
     *
     * @param sysPermissionQueryDTO 查询参数
     * @return 菜单列表
     */
    @SystemCheckPermission("system:menu:query")
    @GetMapping("/list")
    public Result<List<SysPermissionVO>> getMenuList(SysPermissionQueryDTO sysPermissionQueryDTO) {
        return Result.success(sysPermissionService.getAllMenuThree(sysPermissionQueryDTO));
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单id
     * @return 菜单详情
     */
    @SystemCheckPermission("system:menu:detail")
    @GetMapping("/detail")
    public Result<SysPermissionVO> getMenuById(@NotNull(message = "菜单数据id不能为空") Long id) {
        return Result.success("查询成功", sysPermissionService.getPermissionById(id));
    }

    /**
     * 添加菜单
     *
     * @param sysPermissionInsertDTO 菜单信息
     * @return 添加结果
     */
    @Log(title = "菜单管理", logType = LogType.INSERT)
    @SystemCheckPermission("system:menu:add")
    @PostMapping("/add")
    public Result<Void> addMenu(@RequestBody SysPermissionInsertDTO sysPermissionInsertDTO) {
        if (sysPermissionService.insertPermission(sysPermissionInsertDTO)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 修改菜单
     *
     * @param sysPermissionUpdateDTO 菜单信息
     * @return 修改结果
     */
    @Log(title = "菜单管理", logType = LogType.UPDATE)
    @SystemCheckPermission("system:menu:edit")
    @PutMapping("/update")
    public Result<Void> updateMenu(@RequestBody SysPermissionUpdateDTO sysPermissionUpdateDTO) {
        if (sysPermissionService.updatePermission(sysPermissionUpdateDTO)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    /**
     * 删除菜单
     *
     * @param menuIds 菜单id
     * @return 删除结果
     */
    @Log(title = "菜单管理", logType = LogType.DELETE)
    @SystemCheckPermission("system:menu:delete")
    @DeleteMapping("/delete")
    public Result<Void> deleteMenuByIds(Long[] menuIds) {
        sysPermissionService.deletePermissionByIds(menuIds);
        return Result.success("删除成功");
    }
}
