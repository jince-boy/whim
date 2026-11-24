package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import com.whim.core.utils.IdUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO;
import com.whim.system.model.vo.databasemonitor.ConnectionPoolSnapshotVO.Pool;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO.ExecutionSource;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO.MinutePoint;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO.StatementSummary;
import com.whim.system.service.ISysDatabaseMonitorService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 采集本实例的慢 SQL，提供排行、趋势及 HikariCP 连接池指标。
 */
@Service
@RequiredArgsConstructor
@ConfigurationProperties("whim.monitor.database")
public class SysDatabaseMonitorServiceImpl extends SimpleJdbcEventListener implements ISysDatabaseMonitorService {
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_GROUPS_PER_MINUTE = 128;
    private static final StackWalker CALL_STACK = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final StatementKey OVERFLOW = new StatementKey("其他 SQL（本分钟分组数已达上限）", null);
    private static final Comparator<String> OPTIONAL_TEXT = Comparator.nullsLast(Comparator.naturalOrder());
    private static final Comparator<ExecutionSource> SOURCE_ORDER = Comparator.nullsLast(
            Comparator.comparing(ExecutionSource::getRequestMapping, OPTIONAL_TEXT)
                    .thenComparing(ExecutionSource::getControllerMethod, OPTIONAL_TEXT)
                    .thenComparing(ExecutionSource::getCodeLocation, OPTIONAL_TEXT)
                    .thenComparing(ExecutionSource::getMapperMethod, OPTIONAL_TEXT));
    private static final String HIDDEN_TEMPLATE = "其他 SQL（模板未展示）";
    private static final Pattern DML = Pattern.compile("(?i)(?:select|with|insert|update|delete|replace)\\b.*");
    // 只接受可完整识别的 MySQL 业务语句，逐个词元跳过注释并替换内联字面量。
    private static final Pattern TOKEN = Pattern.compile("""
            \\G(?:
                (?<skip> \\s++ | /\\*(?s:.*?)\\*/ | (?:\\#|--(?=\\s|$))[^\\n]*+ )
              | (?<value>
                    (?i:[nxb])?'(?:[^']++|'')*+' | "(?:[^"]++|"")*+"
                  | (?i: 0x[0-9a-f]++ | 0b[01]++ | (?:\\d++(?:\\.\\d*+)?|\\.\\d++)(?:e[+-]?\\d++)?
                       | true\\b | false\\b | null\\b )
                )
              | [a-zA-Z_][a-zA-Z_0-9$]*+ | `[a-zA-Z_][a-zA-Z_0-9$]*+`
              | ->>? | <=> | <> | != | <= | >= | := | << | >> | && | \\|\\|
              | [-+*%(),.;=?<>!&|^~] | /(?!\\*)
            )
            """, Pattern.COMMENTS);

    // P6Spy 在装配数据源时收集监听器，连接池指标注册表延迟到查询时获取。
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;
    private final String instanceId = IdUtils.uuid32();
    private final long collectionStartedAt = System.currentTimeMillis();
    private final NavigableMap<Long, MinuteStatistics> statisticsByMinute = new TreeMap<>();
    private long slowSqlThresholdMillis = 500;
    private MinuteStatistics currentMinute;

    /**
     * 查询所选时段内已完成的 SQL 执行总数及慢 SQL 统计。
     *
     * @param minutes 统计分钟数
     * @return SQL 执行总数、每分钟慢执行次数及按累计耗时排序的模板与来源
     */
    @Override
    public SlowSqlSnapshotVO getSlowSqlSnapshot(int minutes) {
        long now;
        long windowStart;
        long firstMinute;
        long executionCount = 0;
        Map<Long, Map<StatementKey, StatementSummary>> samples = new HashMap<>();
        synchronized (this) {
            now = System.currentTimeMillis();
            long current = Math.floorDiv(now, MINUTE) * MINUTE;
            long oldestMinute = current - 59 * MINUTE;
            statisticsByMinute.headMap(oldestMinute, false).clear();
            if (currentMinute != null && currentMinute.startedAt < oldestMinute) {
                currentMinute = null;
            }
            windowStart = Math.clamp(collectionStartedAt, current - (minutes - 1L) * MINUTE, now);
            firstMinute = Math.floorDiv(windowStart, MINUTE) * MINUTE;
            for (MinuteStatistics statistics : statisticsByMinute.subMap(firstMinute, true, current, true).values()) {
                executionCount += statistics.executionCount;
                // 当前分钟还会写入，复制其模板后即可在锁外汇总整个窗口。
                samples.put(statistics.startedAt, statistics == currentMinute
                        ? Map.copyOf(statistics.statements) : statistics.statements);
            }
        }
        List<MinutePoint> trend = new ArrayList<>(minutes);
        Map<StatementKey, StatementSummary> totals = new HashMap<>();
        for (long minute = firstMinute; minute <= now; minute += MINUTE) {
            long count = 0;
            for (Map.Entry<StatementKey, StatementSummary> entry : samples.getOrDefault(minute, Map.of()).entrySet()) {
                count += entry.getValue().getCount();
                totals.merge(entry.getKey(), entry.getValue(), StatementSummary::merge);
            }
            trend.add(new MinutePoint(minute, count));
        }
        List<StatementSummary> ranking = totals.values().stream()
                .sorted(Comparator.comparingLong(StatementSummary::getTotalDurationMillis).reversed()
                        .thenComparing(StatementSummary::getSqlTemplate)
                        .thenComparing(StatementSummary::getSource, SOURCE_ORDER))
                .limit(100).toList();
        return new SlowSqlSnapshotVO(now, windowStart, slowSqlThresholdMillis, executionCount, trend, ranking);
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
        for (Meter meter : Search.in(meterRegistryProvider.getObject())
                .name(name -> name.startsWith("hikaricp.connections")).meters()) {
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
            } else if (meter instanceof Timer acquire && id.getName().equals("hikaricp.connections.acquire")) {
                // 获取计时器包含成功借用和等待超时，两次瞬时采样之间的短请求也会留下计数。
                pool.setAcquireCount(acquire.count());
            } else if (meter instanceof Counter timeout && id.getName().equals("hikaricp.connections.timeout")) {
                double count = timeout.count();
                pool.setTimeoutCount(Double.isFinite(count) ? (long) count : null);
            }
        }
        return new ConnectionPoolSnapshotVO(System.currentTimeMillis(), instanceId, List.copyOf(pools.values()));
    }

    /**
     * 绑定慢 SQL 阈值，在应用启动时检查配置。
     *
     * @param slowSqlThresholdMillis 计入监控的最短执行耗时，单位毫秒
     */
    public void setSlowSqlThresholdMillis(long slowSqlThresholdMillis) {
        Assert.isTrue(slowSqlThresholdMillis > 0, "慢 SQL 阈值必须大于零");
        this.slowSqlThresholdMillis = slowSqlThresholdMillis;
    }

    /**
     * 统计已完成的 JDBC 执行，包括以异常结束的执行。
     *
     * @param statementInformation JDBC 语句信息
     * @param timeElapsedNanos 执行耗时纳秒数
     * @param exception JDBC 返回的异常
     */
    @Override
    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException exception) {
        recordSqlExecution(statementInformation, timeElapsedNanos, false);
    }

    /**
     * 将整批执行计为一次，达到慢 SQL 阈值时对混合批次单独归类。
     *
     * @param statementInformation 批次语句信息
     * @param timeElapsedNanos 整批执行耗时纳秒数
     * @param updateCounts 各语句影响行数
     * @param exception JDBC 返回的异常
     */
    @Override
    public void onAfterExecuteBatch(StatementInformation statementInformation, long timeElapsedNanos,
                                    int[] updateCounts, SQLException exception) {
        recordSqlExecution(statementInformation, timeElapsedNanos,
                !(statementInformation instanceof PreparedStatementInformation));
    }

    /**
     * 生成稳定的 SQL 模板，遇到无法安全识别的内容时仅返回分类名称。
     *
     * @param sql 绑定参数之前的 JDBC 语句
     * @return 脱敏后的模板或分类名称
     */
    private String getSqlTemplate(String sql) {
        // 转义符的含义随 MySQL 会话模式变化，超长或有歧义的输入统一归类。
        if (sql == null || sql.length() > 16_384 || sql.indexOf('\\') >= 0) {
            return HIDDEN_TEMPLATE;
        }
        Matcher matcher = TOKEN.matcher(sql);
        StringJoiner template = new StringJoiner(" ");
        boolean terminated = false;
        int end = 0;
        while (matcher.find()) {
            end = matcher.end();
            if (matcher.start("skip") >= 0) {
                continue;
            }
            // 终止分号后允许空白和注释；多条语句的总耗时无法归给单一模板。
            if (terminated) {
                return HIDDEN_TEMPLATE;
            }
            if (sql.charAt(matcher.start()) == ';') {
                terminated = true;
                continue;
            }
            template.add(matcher.start("value") >= 0 ? "?" : matcher.group());
            if (template.length() > 2048) {
                return HIDDEN_TEMPLATE;
            }
        }
        String normalized = template.toString();
        return end == sql.length() && DML.matcher(normalized).matches() ? normalized : HIDDEN_TEMPLATE;
    }

    /**
     * 统计当前分钟的 SQL 执行次数，并为达到耗时阈值的执行保留模板和耗时。
     *
     * @param statement 执行完成的 JDBC 语句
     * @param elapsedNanos 执行耗时纳秒数
     * @param mixedBatch 是否为可能包含多种模板的普通 JDBC 批次
     */
    private void recordSqlExecution(StatementInformation statement, long elapsedNanos, boolean mixedBatch) {
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        StatementKey key = null;
        // 普通查询只累加次数，慢执行才在原请求线程和锁外解析模板、路由与调用栈。
        if (durationMillis >= slowSqlThresholdMillis) {
            String template = mixedBatch ? "JDBC 混合批处理" : getSqlTemplate(statement.getSql());
            key = new StatementKey(template, getExecutionSource());
        }
        synchronized (this) {
            long now = System.currentTimeMillis();
            long minute = Math.floorDiv(now, MINUTE) * MINUTE;
            if (currentMinute == null || currentMinute.startedAt != minute) {
                statisticsByMinute.headMap(minute - 59 * MINUTE, false).clear();
                // 回拨时替换重叠分钟，已经交给查询线程的历史统计仍然只读。
                statisticsByMinute.tailMap(minute, true).clear();
                currentMinute = new MinuteStatistics(minute);
                statisticsByMinute.put(minute, currentMinute);
            }
            currentMinute.executionCount++;
            if (key == null) {
                return;
            }
            Map<StatementKey, StatementSummary> statements = currentMinute.statements;
            // 不同调用位置分别计数。溢出组保留次数和耗时，不归给任何一个来源。
            if (statements.size() >= MAX_GROUPS_PER_MINUTE && !statements.containsKey(key)) {
                key = OVERFLOW;
            }
            statements.merge(key, new StatementSummary(key.sqlTemplate, key.source, 1, durationMillis, durationMillis, now),
                    StatementSummary::merge);
        }
    }

    /**
     * 定位执行线程上的项目代码，并从当前 MVC 请求读取控制器方法与路由模板。
     *
     * @return 可定位的调用来源，各项在没有对应上下文时为空
     */
    private ExecutionSource getExecutionSource() {
        HttpServletRequest request = ServletUtils.getRequest();
        String mapping = null;
        String controller = null;
        if (request != null) {
            // 只记录框架匹配的路由模板，不保存 URL 中的业务 ID、查询参数或请求内容。
            if (request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) instanceof String pattern) {
                mapping = request.getMethod() + " " + pattern;
            }
            if (request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) instanceof HandlerMethod handler) {
                controller = handler.getBeanType().getName() + "#" + handler.getMethod().getName();
            }
        }
        StackTraceElement caller = null;
        String mapper = null;
        // 保留真实项目调用点。Mapper 通常由 JDK 代理执行，使用代理实现的业务接口还原方法名。
        List<StackFrame> frames = CALL_STACK.walk(stack -> stack.filter(frame -> frame.getClassName().startsWith("com.whim.")
                || Proxy.isProxyClass(frame.getDeclaringClass())).toList());
        for (StackFrame frame : frames) {
            Class<?> type = frame.getDeclaringClass();
            if (Proxy.isProxyClass(type)) {
                if (mapper == null) {
                    for (Class<?> contract : type.getInterfaces()) {
                        if (contract.getName().startsWith("com.whim.") && BaseMapper.class.isAssignableFrom(contract)) {
                            mapper = contract.getName() + "#" + frame.getMethodName();
                            break;
                        }
                    }
                }
            } else if (caller == null && type != SysDatabaseMonitorServiceImpl.class && !type.isSynthetic()
                    && !frame.getClassName().contains("$$") && !frame.getClassName().startsWith("com.whim.mybatisplus.")) {
                caller = frame.toStackTraceElement();
            }
            if (caller != null && mapper != null) {
                break;
            }
        }
        return new ExecutionSource(mapping, controller, caller == null ? null : caller.toString(), mapper);
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 按 SQL 模板及真实调用来源归并同类慢执行。
     */
    @Value
    private static class StatementKey {
        String sqlTemplate;
        ExecutionSource source;
    }

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 单个自然分钟的全部执行次数和慢 SQL 模板汇总。
     */
    @RequiredArgsConstructor
    private static class MinuteStatistics {
        private final long startedAt;
        private final Map<StatementKey, StatementSummary> statements = new HashMap<>();
        private long executionCount;
    }
}
