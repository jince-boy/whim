package com.whim.system.model.vo.databasemonitor;

import lombok.Value;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前实例在所选自然分钟内的慢 SQL 趋势和累计耗时排行。
 */
@Value
public class SlowSqlSnapshotVO {
    long sampledAt;
    long windowStartedAt;
    long thresholdMillis;
    List<MinutePoint> trend;
    List<StatementSummary> statements;

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 自然分钟的开始时间戳及该分钟已结束的慢 SQL 次数。
     */
    @Value
    public static class MinutePoint {
        long startedAt;
        long count;
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 单一 SQL 模板的汇总，时间和耗时单位均为毫秒。
     */
    @Value
    public static class StatementSummary {
        String sqlTemplate;
        long count;
        long totalDurationMillis;
        long maxDurationMillis;
        long lastOccurredAt;

        /**
         * 合并同一模板在两组执行记录中的次数、耗时与最后发生时间。
         *
         * @param other 同一模板的另一组统计
         * @return 合并后的统计
         */
        public StatementSummary merge(StatementSummary other) {
            return new StatementSummary(sqlTemplate, count + other.count,
                    totalDurationMillis + other.totalDurationMillis, Math.max(maxDurationMillis, other.maxDurationMillis),
                    Math.max(lastOccurredAt, other.lastOccurredAt));
        }
    }
}
