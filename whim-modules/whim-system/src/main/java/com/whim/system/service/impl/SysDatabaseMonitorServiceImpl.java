package com.whim.system.service.impl;

import com.whim.core.utils.IdUtils;
import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO;
import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO.Pool;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO;
import com.whim.system.monitor.SlowSqlMonitor;
import com.whim.system.service.ISysDatabaseMonitorService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 查询本实例的慢 SQL 统计与 HikariCP 连接池指标。
 */
@Service
@RequiredArgsConstructor
public class SysDatabaseMonitorServiceImpl implements ISysDatabaseMonitorService {
    private final SlowSqlMonitor slowSqlMonitor;
    private final MeterRegistry meterRegistry;
    private final String instanceId = IdUtils.uuid32();

    /**
     * 查询所选时段内已完成的慢 SQL 执行。
     *
     * @param minutes 统计分钟数
     * @return 按分钟的次数及按累计耗时排序的模板
     */
    @Override
    public SlowSqlSnapshotVO getSlowSqlSnapshot(int minutes) {
        return slowSqlMonitor.getSlowSqlSnapshot(minutes);
    }

    /**
     * 按池名读取同一实例的 HikariCP 指标。
     *
     * @return 连接池快照
     */
    @Override
    public ConnectionPoolSnapshotVO getConnectionPoolSnapshot() {
        Map<String, Pool> pools = new TreeMap<>();
        // 任一已注册指标都能标识连接池，总连接数被指标过滤器关闭时仍可展示其他读数。
        for (Meter meter : Search.in(meterRegistry).name(name -> name.startsWith("hikaricp.connections")).meters()) {
            Meter.Id id = meter.getId();
            String poolName = id.getTag("pool");
            if (poolName == null) {
                continue;
            }
            Pool pool = pools.computeIfAbsent(poolName, Pool::new);
            if (meter instanceof Gauge gauge) {
                double value = gauge.value();
                Integer count = Double.isFinite(value) ? (int) value : null;
                switch (id.getName()) {
                    case "hikaricp.connections.active" -> pool.setActive(count);
                    case "hikaricp.connections.idle" -> pool.setIdle(count);
                    case "hikaricp.connections.max" -> pool.setMaximum(count);
                    case "hikaricp.connections.pending" -> pool.setPending(count);
                }
            } else if (meter instanceof Counter timeout && id.getName().equals("hikaricp.connections.timeout")) {
                double count = timeout.count();
                pool.setTimeoutCount(Double.isFinite(count) ? (long) count : null);
            }
        }
        return new ConnectionPoolSnapshotVO(System.currentTimeMillis(), instanceId, List.copyOf(pools.values()));
    }
}
