package com.whim.system.service.impl;

import com.whim.core.exception.ServiceException;
import com.whim.system.model.vo.cachemonitor.CacheMonitorSnapshotVO;
import com.whim.system.service.ISysCacheMonitorService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisNode.InfoSection;
import org.redisson.api.redisnode.RedisNodeAsync;
import org.redisson.api.redisnode.RedisNodes;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 通过 Redisson 采集缓存运行指标。
 */
@Service
@RequiredArgsConstructor
public class SysCacheMonitorServiceImpl implements ISysCacheMonitorService {
    private final RedissonClient redissonClient;

    /**
     * 汇总单机或集群主节点的 Redis INFO 快照。
     *
     * @return 本次完整采集的缓存指标
     */
    @Override
    public CacheMonitorSnapshotVO getCacheMonitorSnapshot() {
        boolean cluster = redissonClient.getConfig().isClusterConfig();
        // 集群仅采集主节点，避免复制数据占用的内存重复计入。
        Collection<? extends RedisNodeAsync> nodes = cluster
                ? redissonClient.getRedisNodes(RedisNodes.CLUSTER).getMasters()
                : List.of(redissonClient.getRedisNodes(RedisNodes.SINGLE).getInstance());
        if (nodes.isEmpty()) {
            throw new ServiceException("未发现可采集的 Redis 主节点");
        }

        CacheMonitorSnapshotVO snapshot = new CacheMonitorSnapshotVO();
        try {
            // 并发读取每个主节点一次；缺少任一节点时不返回低估的集群总量。
            List<CompletableFuture<Map<String, String>>> requests = nodes.stream()
                    .map(node -> node.infoAsync(InfoSection.DEFAULT).toCompletableFuture())
                    .toList();
            for (CompletableFuture<Map<String, String>> request : requests) {
                Map<String, String> report = request.join();
                snapshot.setUsedMemoryBytes(addMetric(snapshot.getUsedMemoryBytes(), report, "used_memory"));
                snapshot.setConnectedClients(addMetric(snapshot.getConnectedClients(), report, "connected_clients"));
                snapshot.setCommandsPerSecond(addMetric(snapshot.getCommandsPerSecond(), report, "instantaneous_ops_per_sec"));
                snapshot.setKeyspaceHits(addMetric(snapshot.getKeyspaceHits(), report, "keyspace_hits"));
                snapshot.setKeyspaceMisses(addMetric(snapshot.getKeyspaceMisses(), report, "keyspace_misses"));
                snapshot.setExpiredKeys(addMetric(snapshot.getExpiredKeys(), report, "expired_keys"));
                snapshot.setEvictedKeys(addMetric(snapshot.getEvictedKeys(), report, "evicted_keys"));
            }
        } catch (CompletionException exception) {
            throw new ServiceException("缓存指标采集失败，请检查 Redis 连接及 INFO 命令权限", exception);
        }
        snapshot.setSampledAt(System.currentTimeMillis());
        snapshot.setCluster(cluster);
        snapshot.setPrimaryNodeCount(nodes.size());
        return snapshot;
    }

    /**
     * 将主节点的非负整数指标累加至快照总量。
     *
     * @param total 已采集节点的指标总量
     * @param report 当前主节点返回的指标
     * @param field Redis 指标名称
     * @return 累加后的指标总量
     */
    private long addMetric(long total, Map<String, String> report, String field) {
        try {
            // INFO 计数必须是整数，不能截断小数或让溢出后的负数进入图表。
            long value = Long.parseLong(report.get(field));
            if (value < 0) {
                throw new ServiceException("Redis 监控指标不能为负数：" + field);
            }
            return Math.addExact(total, value);
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new ServiceException("Redis 未返回有效的监控指标：" + field, exception);
        }
    }
}
