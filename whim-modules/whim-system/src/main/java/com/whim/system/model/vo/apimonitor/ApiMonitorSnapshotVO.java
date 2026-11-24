package com.whim.system.model.vo.apimonitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前实例最近五分钟的接口趋势与排行，时间戳单位为毫秒。
 */
@Getter
@RequiredArgsConstructor
public class ApiMonitorSnapshotVO {
    private final long sampledAt;
    private final long windowStartedAt;
    private final List<TrendPoint> trend;
    private final List<Endpoint> endpoints;

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 单个采样区间的请求指标，无请求时平均耗时和错误率为空。
     */
    @Getter
    @RequiredArgsConstructor
    public static class TrendPoint {
        private final long sampledAt;
        private final double requestsPerSecond;
        private final Double averageDurationMillis;
        private final Double serverErrorPercent;
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 时间窗口内同一 HTTP 方法和路由模板的请求统计。
     */
    @Getter
    @RequiredArgsConstructor
    public static class Endpoint {
        private final String method;
        private final String path;
        private final long requestCount;
        private final double averageDurationMillis;
        private final long serverErrorCount;
    }
}
