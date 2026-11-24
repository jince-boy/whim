package com.whim.system.service.impl;

import com.whim.core.exception.ServiceException;
import com.whim.core.utils.ValueParserUtils;
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

        List<Map<String, String>> reports;
        try {
            // 并发读取每个主节点一次；缺少任一节点时不返回低估的集群总量。
            List<CompletableFuture<Map<String, String>>> requests = nodes.stream()
                    .map(node -> node.infoAsync(InfoSection.DEFAULT).toCompletableFuture())
                    .toList();
            reports = requests.stream().map(CompletableFuture::join).toList();
        } catch (CompletionException exception) {
            throw new ServiceException("缓存指标采集失败，请检查 Redis 连接及 INFO 命令权限", exception);
        }
        return CacheMonitorSnapshotVO.builder()
                .sampledAt(System.currentTimeMillis())
                .cluster(cluster)
                .primaryNodeCount(reports.size())
                .usedMemoryBytes(sumMetric(reports, "used_memory"))
                .connectedClients(sumMetric(reports, "connected_clients"))
                .commandsPerSecond(sumMetric(reports, "instantaneous_ops_per_sec"))
                .keyspaceHits(sumMetric(reports, "keyspace_hits"))
                .keyspaceMisses(sumMetric(reports, "keyspace_misses"))
                .expiredKeys(sumMetric(reports, "expired_keys"))
                .evictedKeys(sumMetric(reports, "evicted_keys"))
                .build();
    }

    /**
     * 汇总各主节点的指定指标，缺失的统计值不能视作零。
     *
     * @param reports 各主节点返回的指标
     * @param field Redis 指标名称
     * @return 指标总量
     */
    private long sumMetric(List<Map<String, String>> reports, String field) {
        long total = 0;
        for (Map<String, String> report : reports) {
            Long value = ValueParserUtils.toLong(report.get(field));
            if (value == null) {
                throw new ServiceException("Redis 未返回有效的监控指标：" + field);
            }
            total += value;
        }
        return total;
    }
}
