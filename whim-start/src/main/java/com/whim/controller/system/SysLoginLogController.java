package com.whim.controller.system;


import com.whim.web.annotation.SystemApiPrefix;
import com.whim.core.utils.ExcelUtils;
import com.whim.core.web.Result;
import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.sysLoginLog.SysLoginLogQueryDTO;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统登录日志(SysLoginLog)表控制层
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@SystemApiPrefix
@RestController
@RequestMapping("/loginLog")
@RequiredArgsConstructor
public class SysLoginLogController {
    /**
     * 服务对象
     */
    private final ISysLoginLogService sysLoginLogService;

    /**
     * 分页查询
     *
     * @param sysLoginLogQueryDTO 查询参数
     * @param pageQueryDTO        分页参数
     * @return Result<PageDataVO<SysLoginLog>> 登录日志分页数据
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:loginLog:query")
    public Result<PageDataVO<SysLoginLog>> getLoginLogPage(SysLoginLogQueryDTO sysLoginLogQueryDTO, PageQueryDTO pageQueryDTO) {
        return Result.success("获取成功", sysLoginLogService.getLoginLogPage(sysLoginLogQueryDTO, pageQueryDTO));
    }

    /**
     * 删除登录日志
     *
     * @param loginLogIds 登录日志ID
     * @return Result<Void>
     */
    @Log(title = "登录日志", logType = LogType.DELETE)
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:loginLog:delete")
    public Result<Void> deleteLoginLogByIds(Long[] loginLogIds) {
        sysLoginLogService.deleteLoginLogByIds(loginLogIds);
        return Result.success("删除成功");
    }

    /**
     * 删除所有登录日志
     *
     * @return Result<Void>
     */
    @Log(title = "登录日志", logType = LogType.CLEAN)
    @DeleteMapping("/clean")
    @SystemCheckPermission("system:loginLog:clean")
    public Result<Void> deleteLoginLogAll() {
        sysLoginLogService.deleteLoginLogAll();
        return Result.success("删除成功");
    }

    /**
     * 导出登录日志
     *
     * @param response 响应
     */
    @Log(title = "登录日志", logType = LogType.EXPORT)
    @GetMapping("/export")
    @SystemCheckPermission("system:loginLog:export")
    public void exportLoginLog(HttpServletResponse response) {
        ExcelUtils.exportExcel(sysLoginLogService.getLoginLogList(), SysLoginLog.class)
                .autoColumnWidth()
                .fileName("登录日志")
                .toResponse(response);
    }
}

