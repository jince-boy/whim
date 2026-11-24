package com.whim.system.monitor;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO.MinutePoint;
import com.whim.system.model.vo.databasemonitor.SlowSqlSnapshotVO.StatementSummary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
 * @description 从 P6Spy 执行完成事件采集慢 SQL，在本实例保留最近六十个自然分钟。
 */
@Component
@ConfigurationProperties("whim.monitor.database")
public class SlowSqlMonitor extends SimpleJdbcEventListener {
    private static final long MINUTE = TimeUnit.MINUTES.toMillis(1);
    private static final int MAX_TEMPLATES_PER_MINUTE = 128;
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
    private long slowSqlThresholdMillis = 500;
    private final long collectionStartedAt = System.currentTimeMillis();
    private final NavigableMap<Long, Map<String, StatementSummary>> minutes = new TreeMap<>();

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
     * 对达到阈值的 JDBC 执行计数，包括以异常结束的执行。
     *
     * @param statementInformation JDBC 语句信息
     * @param timeElapsedNanos 执行耗时纳秒数
     * @param exception JDBC 返回的异常
     */
    @Override
    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException exception) {
        addSlowSql(statementInformation, timeElapsedNanos, false);
    }

    /**
     * 将一次批执行计为一次慢 SQL，普通 Statement 的混合批次单独归类。
     *
     * @param statementInformation 批次语句信息
     * @param timeElapsedNanos 整批执行耗时纳秒数
     * @param updateCounts 各语句影响行数
     * @param exception JDBC 返回的异常
     */
    @Override
    public void onAfterExecuteBatch(StatementInformation statementInformation, long timeElapsedNanos,
                                    int[] updateCounts, SQLException exception) {
        addSlowSql(statementInformation, timeElapsedNanos, !(statementInformation instanceof PreparedStatementInformation));
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
     * 将一次执行加入当前分钟，限制高基数 SQL 占用的内存。
     *
     * @param statement 执行完成的 JDBC 语句
     * @param elapsedNanos 执行耗时纳秒数
     * @param mixedBatch 是否为可能包含多种模板的普通 JDBC 批次
     */
    private void addSlowSql(StatementInformation statement, long elapsedNanos, boolean mixedBatch) {
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        if (durationMillis < slowSqlThresholdMillis) {
            return;
        }
        // 在持锁前处理绑定前的语句模板，普通批次无法拆分每条语句的耗时。
        String template = mixedBatch ? "JDBC 混合批处理" : getSqlTemplate(statement.getSql());
        synchronized (this) {
            long now = System.currentTimeMillis();
            long minute = Math.floorDiv(now, MINUTE) * MINUTE;
            minutes.headMap(minute - 59 * MINUTE, false).clear();
            // 回拨时重建重叠分钟，使已提供给查询线程的历史桶始终只读。
            if (!minutes.isEmpty() && minute < minutes.lastKey()) {
                minutes.tailMap(minute, true).clear();
            }
            Map<String, StatementSummary> statements = minutes.computeIfAbsent(minute, ignored -> new HashMap<>());
            // 超量模板仍计入次数和耗时，让趋势保留该分钟的全部慢执行。
            if (statements.size() >= MAX_TEMPLATES_PER_MINUTE && !statements.containsKey(template)) {
                template = "其他 SQL（本分钟模板数已达上限）";
            }
            statements.merge(template, new StatementSummary(template, 1, durationMillis, durationMillis, now),
                    StatementSummary::merge);
        }
    }

    /**
     * 汇总指定分钟数的趋势和累计慢执行耗时最高的一百个模板。
     *
     * @param windowMinutes 包含当前分钟的统计时长
     * @return 慢 SQL 统计快照
     */
    public SlowSqlSnapshotVO getSlowSqlSnapshot(int windowMinutes) {
        long now;
        long windowStart;
        long firstMinute;
        NavigableMap<Long, Map<String, StatementSummary>> samples;
        synchronized (this) {
            now = System.currentTimeMillis();
            long current = Math.floorDiv(now, MINUTE) * MINUTE;
            minutes.headMap(current - 59 * MINUTE, false).clear();
            windowStart = Math.clamp(collectionStartedAt, current - (windowMinutes - 1L) * MINUTE, now);
            firstMinute = Math.floorDiv(windowStart, MINUTE) * MINUTE;
            samples = new TreeMap<>(minutes.subMap(firstMinute, true, current, true));
            // 只有最新的桶还可能写入。历史桶及其统计项已固定，可在锁外直接汇总。
            if (!samples.isEmpty()) {
                long latest = samples.lastKey();
                samples.put(latest, Map.copyOf(samples.get(latest)));
            }
        }
        List<MinutePoint> trend = new ArrayList<>(windowMinutes);
        Map<String, StatementSummary> totals = new HashMap<>();
        for (long minute = firstMinute; minute <= now; minute += MINUTE) {
            long count = 0;
            for (StatementSummary entry : samples.getOrDefault(minute, Map.of()).values()) {
                count += entry.getCount();
                totals.merge(entry.getSqlTemplate(), entry, StatementSummary::merge);
            }
            trend.add(new MinutePoint(minute, count));
        }
        List<StatementSummary> ranking = totals.values().stream()
                .sorted(Comparator.comparingLong(StatementSummary::getTotalDurationMillis).reversed()
                        .thenComparing(StatementSummary::getSqlTemplate))
                .limit(100).toList();
        return new SlowSqlSnapshotVO(now, windowStart, slowSqlThresholdMillis, trend, ranking);
    }
}
