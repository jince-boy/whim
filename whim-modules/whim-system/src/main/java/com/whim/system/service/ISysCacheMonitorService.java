package com.whim.system.service;

import com.whim.system.model.vo.cachemonitor.CacheMonitorSnapshotVO;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 系统缓存监控服务。
 */
public interface ISysCacheMonitorService {
    /**
     * 查询当前 Redis 部署的主节点运行指标。
     *
     * @return 缓存监控快照
     */
    CacheMonitorSnapshotVO getCacheMonitorSnapshot();
}
