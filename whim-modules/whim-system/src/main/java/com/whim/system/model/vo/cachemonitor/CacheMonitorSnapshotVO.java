package com.whim.system.model.vo.cachemonitor;

import lombok.Builder;
import lombok.Getter;

/**
 * @author Jince
 * @date 2026/11/24
 * @description Redis 主节点运行指标的汇总快照。
 */
@Getter
@Builder
public class CacheMonitorSnapshotVO {
    /** 采集完成时的 Unix 毫秒时间戳。 */
    private final long sampledAt;
    private final boolean cluster;
    private final int primaryNodeCount;
    private final long usedMemoryBytes;
    private final long connectedClients;
    private final long commandsPerSecond;
    private final long keyspaceHits;
    private final long keyspaceMisses;
    private final long expiredKeys;
    private final long evictedKeys;
}
