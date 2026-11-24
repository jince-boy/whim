package com.whim.system.service.impl;

import com.whim.system.model.vo.apimonitor.ApiMonitorSnapshotVO;
import com.whim.system.model.vo.apimonitor.ApiMonitorSnapshotVO.Endpoint;
import com.whim.system.model.vo.apimonitor.ApiMonitorSnapshotVO.TrendPoint;
import com.whim.system.service.ISysApiMonitorService;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 从 Actuator 请求计时器采样，在本实例内保留最近五分钟的接口统计。
 */
@Service
@RequiredArgsConstructor
public class SysApiMonitorServiceImpl implements ISysApiMonitorService {
    private static final long WINDOW_MILLIS = Duration.ofMinutes(5).toMillis();
    private static final long SAMPLE_SECONDS = 5;
    private static final double NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);
    private final MeterRegistry meterRegistry;
    private final Deque<SampleInterval> intervals = new ArrayDeque<>();
    private final Map<EndpointKey, RequestTotals> windowTotals = new HashMap<>();
    private Map<Meter.Id, RequestTotals> baseline = Map.of();
    private long baselineTime;

    /**
     * 查询最近五分钟的接口指标，包含上次定时采样后已完成的请求。
     *
     * @return 当前实例的接口监控快照
     */
    @Override
    public ApiMonitorSnapshotVO getApiMonitorSnapshot() {
        long sampledAt;
        long windowStartedAt;
        Map<EndpointKey, RequestTotals> totals;
        List<TrendPoint> trend;
        synchronized (this) {
            sampledAt = System.currentTimeMillis();
            SampleInterval current = readApiInterval(sampledAt, false);
            windowStartedAt = intervals.isEmpty() ? baselineTime : intervals.getFirst().startedAt;
            totals = new HashMap<>(windowTotals);
            trend = new ArrayList<>(intervals.size() + 1);
            for (SampleInterval interval : intervals) {
                trend.add(interval.point);
            }
            // 只把未归档区间叠加到本次响应上，连续刷新不会改变下一次定时采样的基线。
            if (current != null) {
                trend.add(current.point);
                current.endpoints.forEach((key, delta) -> totals.merge(key, delta, RequestTotals::plus));
            }
        }
        List<Endpoint> ranking = totals.entrySet().stream()
                .map(entry -> {
                    RequestTotals values = entry.getValue();
                    return new Endpoint(entry.getKey().method, entry.getKey().path, values.count,
                            values.durationNanos / NANOS_PER_MILLI / values.count, values.errors);
                })
                .sorted(Comparator.comparingDouble(Endpoint::getAverageDurationMillis).reversed()
                        .thenComparing(Endpoint::getPath).thenComparing(Endpoint::getMethod))
                .toList();
        return new ApiMonitorSnapshotVO(sampledAt, windowStartedAt, trend, ranking);
    }

    /**
     * 每五秒将完整区间计入历史趋势和排行。
     */
    @Scheduled(fixedDelay = SAMPLE_SECONDS, timeUnit = TimeUnit.SECONDS)
    public synchronized void sampleApiRequests() {
        SampleInterval completed = readApiInterval(System.currentTimeMillis(), true);
        if (completed != null) {
            intervals.addLast(completed);
            completed.endpoints.forEach((key, delta) -> windowTotals.merge(key, delta, RequestTotals::plus));
        }
    }

    /**
     * 读取各计时器相对采样基线的增量，并移出已过期的区间。
     *
     * @param sampledAt 读取时间戳
     * @param commit 是否推进定时采样基线
     * @return 当前区间，建立基线或时间未推进时为空
     */
    private SampleInterval readApiInterval(long sampledAt, boolean commit) {
        long startedAt = baselineTime;
        long elapsed = sampledAt - startedAt;
        if (elapsed == 0) {
            return null;
        }
        // 首次采样、时钟回拨或长时间中断时，从本次读数重新开始。
        boolean reset = elapsed < 0 || elapsed > TimeUnit.SECONDS.toMillis(SAMPLE_SECONDS * 3);
        Map<Meter.Id, RequestTotals> nextBaseline = commit || reset ? new HashMap<>() : null;
        Map<EndpointKey, RequestTotals> endpoints = new HashMap<>();
        long requestCount = 0;
        long durationNanos = 0;
        long errorCount = 0;
        for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
            Meter.Id id = timer.getId();
            String path = id.getTag("uri");
            // 只统计系统路由模板，排除监控自身的轮询。
            if (path == null || !path.startsWith("/system/") || path.equals("/system/monitor/api/snapshot")) {
                continue;
            }
            long count = timer.count();
            long nanos = Math.round(timer.totalTime(TimeUnit.NANOSECONDS));
            RequestTotals previous = baseline.get(id);
            if (previous != null && count == previous.count && nanos == previous.durationNanos) {
                if (nextBaseline != null) {
                    nextBaseline.put(id, previous);
                }
                continue;
            }
            String status = id.getTag("status");
            RequestTotals reading = new RequestTotals(count, nanos, status != null && status.startsWith("5") ? count : 0);
            if (nextBaseline != null) {
                nextBaseline.put(id, reading);
            }
            if (reset) {
                continue;
            }
            // 保留原始标签计算差值，同一路由不同状态码的计时器可以独立重置。
            RequestTotals delta = previous != null && count >= previous.count && nanos >= previous.durationNanos
                    ? reading.minus(previous) : reading;
            if (delta.count > 0) {
                EndpointKey key = new EndpointKey(id.getTag("method"), path);
                endpoints.merge(key, delta, RequestTotals::plus);
                requestCount += delta.count;
                durationNanos += delta.durationNanos;
                errorCount += delta.errors;
            }
        }
        if (nextBaseline != null) {
            baseline = nextBaseline;
            baselineTime = sampledAt;
        }
        if (reset) {
            intervals.clear();
            windowTotals.clear();
            return null;
        }
        // 每份区间增量只加减一次；请求数归零时移除该接口。
        while (!intervals.isEmpty() && intervals.getFirst().startedAt < sampledAt - WINDOW_MILLIS) {
            intervals.removeFirst().endpoints.forEach((key, expired) ->
                    windowTotals.compute(key, (ignored, sum) -> sum.count == expired.count ? null : sum.minus(expired)));
        }
        TrendPoint point = new TrendPoint(sampledAt, requestCount * 1000.0 / elapsed,
                requestCount == 0 ? null : durationNanos / NANOS_PER_MILLI / requestCount,
                requestCount == 0 ? null : errorCount * 100.0 / requestCount);
        return new SampleInterval(startedAt, point, endpoints);
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 区分接口的 HTTP 方法与路由模板。
     */
    @Value
    private static class EndpointKey {
        String method;
        String path;
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 不可变的请求计数，使用纳秒保证窗口滚动加减时的精度。
     */
    @RequiredArgsConstructor
    private static class RequestTotals {
        private final long count;
        private final long durationNanos;
        private final long errors;

        /**
         * 合并两组已完成请求。
         *
         * @param other 需要计入的统计
         * @return 合并后的统计
         */
        private RequestTotals plus(RequestTotals other) {
            return new RequestTotals(count + other.count, durationNanos + other.durationNanos, errors + other.errors);
        }

        /**
         * 扣除基线读数或过期区间。
         *
         * @param other 需要扣除的统计
         * @return 剩余的统计
         */
        private RequestTotals minus(RequestTotals other) {
            return new RequestTotals(count - other.count, durationNanos - other.durationNanos, errors - other.errors);
        }
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 从上次定时采样到本次读取的趋势点和有流量的接口增量。
     */
    @RequiredArgsConstructor
    private static class SampleInterval {
        private final long startedAt;
        private final TrendPoint point;
        private final Map<EndpointKey, RequestTotals> endpoints;
    }
}
