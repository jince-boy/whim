package com.whim.system.service;

import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 系统数据库监控查询。
 */
public interface ISysDatabaseMonitorService {
    /**
     * 查询所选时段内的 SQL 执行总数、慢 SQL 次数趋势及累计耗时排行。
     *
     * @param minutes 统计分钟数，包含当前分钟
     * @return 当前实例的慢 SQL 快照
     */
    SlowSqlSnapshotVO getSlowSqlSnapshot(int minutes);

    /**
     * 查询当前实例各连接池的占用、等待、累计获取次数及超时次数。
     *
     * @return 连接池快照
     */
    ConnectionPoolSnapshotVO getConnectionPoolSnapshot();
}
