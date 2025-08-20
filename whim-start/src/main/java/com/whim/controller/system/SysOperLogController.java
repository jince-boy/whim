package com.whim.controller.system;


import com.whim.core.annotation.SystemApiPrefix;
import com.whim.core.utils.ExcelUtils;
import com.whim.core.web.Result;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysOperLog.SysOperLogQueryDTO;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.service.ISysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志(SysOperLog)表控制层
 *
 * @author Jince
 * @since 2025-08-19 21:46:45
 */
@SystemApiPrefix
@RestController
@RequestMapping("/operLog")
@RequiredArgsConstructor
public class SysOperLogController {
    private final ISysOperLogService sysOperLogService;

    /**
     * 获取操作日志分页
     *
     * @param sysOperLogQueryDTO 查询参数
     * @param pageQueryDTO       分页参数
     * @return 操作日志分页
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:operLog:query")
    public Result<PageDataVO<SysOperLog>> getOperLogPage(SysOperLogQueryDTO sysOperLogQueryDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("获取成功", sysOperLogService.getOperLogPage(sysOperLogQueryDTO, pageQueryDTO));
    }

    /**
     * 删除操作日志
     *
     * @param operLogIds 操作日志ID
     * @return 删除结果
     */
    @Log(title = "操作日志", logType = LogType.DELETE)
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:operLog:delete")
    public Result<Void> deleteOperLogByIds(Long[] operLogIds) {
        sysOperLogService.deleteOperLogByIds(operLogIds);
        return Result.success("删除成功");
    }

    /**
     * 清空操作日志
     *
     * @return 清空结果
     */
    @Log(title = "操作日志", logType = LogType.CLEAN)
    @DeleteMapping("/clean")
    @SystemCheckPermission("system:operLog:clean")
    public Result<Void> cleanOperLog() {
        sysOperLogService.cleanOperLog();
        return Result.success("清空成功");
    }

    /**
     * 导出操作日志
     *
     * @param response 响应
     */
    @Log(title = "操作日志", logType = LogType.EXPORT)
    @GetMapping("/export")
    @SystemCheckPermission("system:operLog:export")
    public void exportOperLog(HttpServletResponse response) {
        ExcelUtils.exportExcel(sysOperLogService.getOperLogList(), SysOperLog.class)
                .autoColumnWidth()
                .fileName("登录日志")
                .toResponse(response);
    }
}

