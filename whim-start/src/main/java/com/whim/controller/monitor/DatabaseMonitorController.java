package com.whim.controller.monitor;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO;
import com.whim.system.service.ISysDatabaseMonitorService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 数据库慢 SQL 与连接池监控接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/monitor/database")
public class DatabaseMonitorController {
    private final ISysDatabaseMonitorService databaseMonitorService;

    /**
     * 查询所选时段的慢 SQL 次数趋势和模板排行。
     *
     * @param minutes 统计分钟数
     * @param response 用于控制快照缓存的响应
     * @return 慢 SQL 快照
     */
    @GetMapping("/slowSql")
    @SystemCheckPermission("monitor:database:query")
    public Result<SlowSqlSnapshotVO> getSlowSqlSnapshot(
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "统计时段至少为一分钟")
            @Max(value = 60, message = "统计时段最多为六十分钟") int minutes, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success("慢 SQL 查询成功", databaseMonitorService.getSlowSqlSnapshot(minutes));
    }

    /**
     * 查询当前实例各 HikariCP 连接池的实时指标。
     *
     * @param response 用于控制快照缓存的响应
     * @return 连接池快照
     */
    @GetMapping("/connectionPool")
    @SystemCheckPermission("monitor:database:query")
    public Result<ConnectionPoolSnapshotVO> getConnectionPoolSnapshot(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success("连接池查询成功", databaseMonitorService.getConnectionPoolSnapshot());
    }
}
