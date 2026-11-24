package com.whim.controller.monitor;

import com.whim.satoken.annotation.SystemCheckLogin;
import com.whim.satoken.annotation.SystemCheckPermission;
import com.whim.system.model.vo.cachemonitor.CacheMonitorSnapshotVO;
import com.whim.system.service.ISysCacheMonitorService;
import com.whim.web.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 系统监控缓存指标查询接口。
 */
@RestController
@RequiredArgsConstructor
@SystemCheckLogin
@RequestMapping("/system/monitor/cache")
public class CacheMonitorController {
    private final ISysCacheMonitorService cacheMonitorService;

    /**
     * 查询当前 Redis 实例或集群主节点的汇总指标。
     *
     * @return 缓存监控快照
     */
    @GetMapping("/snapshot")
    @SystemCheckPermission("monitor:cache:query")
    public Result<CacheMonitorSnapshotVO> getCacheMonitorSnapshot() {
        return Result.success("缓存监控查询成功", cacheMonitorService.getCacheMonitorSnapshot());
    }
}
