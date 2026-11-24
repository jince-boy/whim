package com.whim.system.model.vo.databasemonitor;

import lombok.Value;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前实例在所选自然分钟内的 SQL 执行总数、慢 SQL 趋势和累计耗时排行。
 */
@Value
public class SlowSqlSnapshotVO {
    long sampledAt;
    long windowStartedAt;
    long thresholdMillis;
    long executionCount;
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
     * @description 同一来源执行同一 SQL 模板的统计，时间和耗时单位均为毫秒。
     */
    @Value
    public static class StatementSummary {
        String sqlTemplate;
        ExecutionSource source;
        long count;
        long totalDurationMillis;
        long maxDurationMillis;
        long lastOccurredAt;

        /**
         * 合并同一模板、同一来源在两个统计区间的执行记录。
         *
         * @param other 同一模板及来源的另一组统计
         * @return 合并后的统计
         */
        public StatementSummary merge(StatementSummary other) {
            return new StatementSummary(sqlTemplate, source, count + other.count,
                    totalDurationMillis + other.totalDurationMillis, Math.max(maxDurationMillis, other.maxDurationMillis),
                    Math.max(lastOccurredAt, other.lastOccurredAt));
        }
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 慢执行所在的请求路由、控制器、项目调用位置及调用栈中的 Mapper 方法。
     */
    @Value
    public static class ExecutionSource {
        String requestMapping;
        String controllerMethod;
        String codeLocation;
        String mapperMethod;
    }
}
