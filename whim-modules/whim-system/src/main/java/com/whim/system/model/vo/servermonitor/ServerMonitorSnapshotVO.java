package com.whim.system.model.vo.servermonitor;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前后端实例可见的服务器资源快照。
 */
@Getter
@Builder
public class ServerMonitorSnapshotVO {
    private final long sampledAt;
    private final String instanceId;
    private final String hostName;
    private final String operatingSystem;
    private final String architecture;
    private final long uptimeSeconds;
    private final int availableProcessors;
    /** CPU 使用率，单位为百分比，平台尚无读数时为空。 */
    private final Double cpuUsagePercent;
    private final long totalMemoryBytes;
    private final long freeMemoryBytes;
    private final List<Disk> disks;
    private final List<NetworkInterface> networkInterfaces;

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 本地挂载卷容量，可用空间按当前进程权限统计。
     */
    @Getter
    @RequiredArgsConstructor
    public static class Disk {
        private final String mount;
        private final String type;
        private final long totalBytes;
        private final long freeBytes;
        private final long usableBytes;
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 网卡累计收发字节数。
     */
    @Getter
    @RequiredArgsConstructor
    public static class NetworkInterface {
        private final String name;
        private final int index;
        private final long receivedBytes;
        private final long sentBytes;
    }
}
