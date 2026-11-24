package com.whim.controller.monitor;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.vo.apimonitor.ApiMonitorSnapshotVO;
import com.whim.system.service.ISysApiMonitorService;
import com.whim.web.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 系统接口监控查询入口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/monitor/api")
public class ApiMonitorController {
    private final ISysApiMonitorService apiMonitorService;

    /**
     * 实时查询当前实例最近五分钟的请求趋势和慢接口排行。
     *
     * @param response 用于控制快照缓存的响应
     * @return 接口监控快照
     */
    @GetMapping("/snapshot")
    @SystemCheckPermission("monitor:api:query")
    public Result<ApiMonitorSnapshotVO> getApiMonitorSnapshot(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success("接口监控查询成功", apiMonitorService.getApiMonitorSnapshot());
    }
}
