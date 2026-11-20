package com.whim.controller.system;

import com.whim.log.annotation.Log;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.dto.loginlog.SysLoginLogQueryDTO;
import com.whim.system.model.vo.loginlog.SysLoginLogVO;
import com.whim.system.service.ISysLoginLogService;
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
 * @description 登录日志管理接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/loginLog")
public class SysLoginLogController {
    private final ISysLoginLogService loginLogService;

    /**
     * 按筛选条件分页查询登录日志。
     *
     * @param query 登录日志筛选及分页条件
     * @return 登录日志查询结果
     */
    @GetMapping("/page")
    @SystemCheckPermission("system:loginLog:page")
    public Result<PageDataVO<SysLoginLogVO>> getLoginLogPage(@Valid SysLoginLogQueryDTO query) {
        return Result.success("登录日志查询成功", loginLogService.getLoginLogPage(query));
    }

    /**
     * 逻辑删除所选登录日志。
     *
     * @param loginLogIds 所选登录日志主键
     * @return 删除结果
     */
    @Log(title = "登录日志", type = LogType.DELETE)
    @DeleteMapping("/delete")
    @SystemCheckPermission("system:loginLog:delete")
    public Result<Void> deleteLoginLogs(
            @RequestParam("loginLogIds") @NotEmpty(message = "请选择登录日志")
            List<@NotNull(message = "日志ID不能为空") Long> loginLogIds) {
        loginLogService.deleteLoginLogs(loginLogIds);
        return Result.success("登录日志删除成功");
    }

    /**
     * 清空本次操作可见的全部登录日志。
     *
     * @return 清空结果
     */
    @Log(title = "登录日志", type = LogType.CLEAN)
    @DeleteMapping("/clean")
    @SystemCheckPermission("system:loginLog:clean")
    public Result<Void> clearLoginLogs() {
        loginLogService.clearLoginLogs();
        return Result.success("登录日志清空成功");
    }

    /**
     * 按筛选条件分批导出登录日志。
     *
     * @param query 登录日志筛选条件
     * @param response 下载响应
     */
    @Log(title = "登录日志", type = LogType.EXPORT)
    @GetMapping("/export")
    @SystemCheckPermission("system:loginLog:export")
    public void exportLoginLogs(@Valid SysLoginLogQueryDTO query, HttpServletResponse response) {
        loginLogService.exportLoginLogs(query, response);
    }
}
