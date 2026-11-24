package com.whim.system.model.vo.cachemonitor;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/24
 * @description Redis 主节点运行指标的汇总快照。
 */
@Data
public class CacheMonitorSnapshotVO {
    /** 采集完成时的 Unix 毫秒时间戳。 */
    private long sampledAt;
    private boolean cluster;
    private int primaryNodeCount;
    private long usedMemoryBytes;
    private long connectedClients;
    private long commandsPerSecond;
    private long keyspaceHits;
    private long keyspaceMisses;
    private long expiredKeys;
    private long evictedKeys;
}
