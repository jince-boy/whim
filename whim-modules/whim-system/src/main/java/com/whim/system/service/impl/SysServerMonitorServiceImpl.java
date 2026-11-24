package com.whim.system.service.impl;

import com.sun.management.OperatingSystemMXBean;
import com.whim.core.utils.IdUtils;
import com.whim.system.model.vo.servermonitor.ServerMonitorSnapshotVO;
import com.whim.system.model.vo.servermonitor.ServerMonitorSnapshotVO.Disk;
import com.whim.system.model.vo.servermonitor.ServerMonitorSnapshotVO.NetworkInterface;
import com.whim.system.service.ISysServerMonitorService;
import org.springframework.stereotype.Service;
import oshi.ffm.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 采集运行环境的 CPU、内存、本地磁盘和网卡指标。
 */
@Service
public class SysServerMonitorServiceImpl implements ISysServerMonitorService {
    private final SystemInfo systemInfo = new SystemInfo();
    private final OperatingSystemMXBean operatingSystem = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final String instanceId = IdUtils.uuid32();
    private ServerMonitorSnapshotVO lastSnapshot;
    private long lastSampleNanos;

    /**
     * 获取当前实例的资源快照，并在短时间内复用已完成的采样。
     *
     * @return 带采样时间和实例标识的服务器监控快照
     */
    @Override
    public synchronized ServerMonitorSnapshotVO getServerMonitorSnapshot() {
        // 多个监控页同时刷新时只采一次，避免重复枚举设备或干扰 CPU 采样间隔。
        if (lastSnapshot != null && System.nanoTime() - lastSampleNanos < TimeUnit.SECONDS.toNanos(2)) {
            return lastSnapshot;
        }
        OperatingSystem system = systemInfo.getOperatingSystem();
        // 仅枚举本地挂载卷，离线设备和无容量的伪文件系统不参与容量展示。
        List<Disk> disks = system.getFileSystem().getFileStores(true).stream()
                .filter(disk -> disk.getTotalSpace() > 0)
                .map(disk -> new Disk(disk.getMount(), disk.getType(), disk.getTotalSpace(),
                        disk.getFreeSpace(), disk.getUsableSpace()))
                .toList();
        List<NetworkInterface> interfaces = systemInfo.getHardware().getNetworkIFs().stream()
                .map(network -> new NetworkInterface(network.getName(), network.getIndex(),
                        network.getBytesRecv(), network.getBytesSent()))
                .toList();

        // JDK 按进程可见的运行环境统计 CPU 和内存，容器限额应当反映在读数中。
        double cpuLoad = operatingSystem.getCpuLoad();
        lastSnapshot = ServerMonitorSnapshotVO.builder()
                .sampledAt(System.currentTimeMillis()).instanceId(instanceId)
                .hostName(system.getNetworkParams().getHostName())
                .operatingSystem(operatingSystem.getName() + " " + operatingSystem.getVersion())
                .architecture(operatingSystem.getArch()).uptimeSeconds(system.getSystemUptime())
                .availableProcessors(operatingSystem.getAvailableProcessors())
                .cpuUsagePercent(cpuLoad >= 0 && cpuLoad <= 1 ? cpuLoad * 100 : null)
                .totalMemoryBytes(operatingSystem.getTotalMemorySize()).freeMemoryBytes(operatingSystem.getFreeMemorySize())
                .disks(disks).networkInterfaces(interfaces).build();
        lastSampleNanos = System.nanoTime();
        return lastSnapshot;
    }
}
