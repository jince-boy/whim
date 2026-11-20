package com.whim.controller.system;

import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.operlog.SysOperLogQueryDTO;
import com.whim.system.model.vo.operlog.SysOperLogDetailVO;
import com.whim.system.model.vo.operlog.SysOperLogVO;
import com.whim.system.service.ISysOperLogService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 操作日志管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/operLog")
public class SysOperLogController {
    private final ISysOperLogService operLogService;

    /**
     * 按筛选条件分页查询操作日志。
     *
     * @param query 操作日志筛选及分页条件
     * @return 操作日志查询结果
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:operLog:page")
    public Result<PageDataVO<SysOperLogVO>> getOperLogPage(@Valid SysOperLogQueryDTO query) {
        return Result.success("操作日志查询成功", operLogService.getOperLogPage(query));
    }

    /**
     * 根据主键查询操作日志详情。
     *
     * @param id 操作日志主键
     * @return 操作日志详情
     */
    @GetMapping("/detail")
    @SystemCheckPermission("system:operLog:detail")
    public Result<SysOperLogDetailVO> getOperLogDetail(@RequestParam("id") Long id) {
        return Result.success("操作日志详情查询成功", operLogService.getOperLogDetail(id));
    }

    /**
     * 逻辑删除所选操作日志。
     *
     * @param operLogIds 所选操作日志主键
     * @return 删除结果
     */
    @Log(title = "操作日志", type = LogType.DELETE)
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:operLog:delete")
    public Result<Void> deleteOperLogs(
            @RequestParam("operLogIds") @NotEmpty(message = "请选择操作日志")
            List<@NotNull(message = "日志ID不能为空") Long> operLogIds) {
        operLogService.removeByIds(operLogIds);
        return Result.success("操作日志删除成功");
    }

    /**
     * 清空本次操作可见的全部操作日志。
     *
     * @return 清空结果
     */
    @Log(title = "操作日志", type = LogType.CLEAN)
    @DeleteMapping("/clean")
    @SystemCheckPermission("system:operLog:clean")
    public Result<Void> clearOperLogs() {
        operLogService.clearOperLogs();
        return Result.success("操作日志清空成功");
    }

    /**
     * 按筛选条件分批导出操作日志。
     *
     * @param query 操作日志筛选条件
     * @param response 下载响应
     */
    @Log(title = "操作日志", type = LogType.EXPORT)
    @GetMapping("/export")
    @SystemCheckPermission("system:operLog:export")
    public void exportOperLogs(@Valid SysOperLogQueryDTO query, HttpServletResponse response) {
        operLogService.exportOperLogs(query, response);
    }
}
