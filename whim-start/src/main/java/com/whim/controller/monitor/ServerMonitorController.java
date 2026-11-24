package com.whim.controller.monitor;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.vo.servermonitor.ServerMonitorSnapshotVO;
import com.whim.system.service.ISysServerMonitorService;
import com.whim.web.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 系统监控服务器指标查询接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/monitor/server")
public class ServerMonitorController {
    private final ISysServerMonitorService serverMonitorService;

    /**
     * 查询当前后端实例的服务器资源快照。
     *
     * @return 服务器监控快照
     */
    @GetMapping("/snapshot")
    @SystemCheckPermission("monitor:server:query")
    public Result<ServerMonitorSnapshotVO> getServerMonitorSnapshot() {
        return Result.success("服务器监控查询成功", serverMonitorService.getServerMonitorSnapshot());
    }
}
