package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/3/24
 * @description 日期时间工具类，内部统一基于 {@code java.time} 实现，并保留常见旧版时间类型的兼容转换能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    /**
     * 标准日期格式。
     */
    public static final String DATE_PATTERN = "uuuu-MM-dd";

    /**
     * 标准时间格式。
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 标准日期时间格式。
     */
    public static final String DATE_TIME_PATTERN = "uuuu-MM-dd HH:mm:ss";

    /**
     * 紧凑日期时间格式。
     */
    public static final String COMPACT_DATE_TIME_PATTERN = "uuuuMMddHHmmss";

    /**
     * 带毫秒的日期时间格式。
     */
    public static final String MILLIS_DATE_TIME_PATTERN = "uuuu-MM-dd HH:mm:ss.SSS";

    /**
     * 紧凑日期格式。
     */
    public static final String COMPACT_DATE_PATTERN = "uuuuMMdd";

    /**
     * 紧凑时间格式。
     */
    public static final String COMPACT_TIME_PATTERN = "HHmmss";

    /**
     * 斜杠日期格式。
     */
    public static final String SLASH_DATE_PATTERN = "uuuu/MM/dd";

    /**
     * 带空格的紧凑日期时间格式。
     */
    public static final String COMPACT_DATE_TIME_WITH_SPACE_PATTERN = "uuuuMMdd HH:mm:ss";

    /**
     * 年月格式。
     */
    public static final String YEAR_MONTH_PATTERN = "uuuuMM";

    /**
     * 年份格式。
     */
    public static final String YEAR_PATTERN = "uuuu";

    /**
     * 标准日期格式化器。
     */
    public static final DateTimeFormatter DATE_FORMATTER = formatter(DATE_PATTERN);

    /**
     * 标准时间格式化器。
     */
    public static final DateTimeFormatter TIME_FORMATTER = formatter(TIME_PATTERN);

    /**
     * 标准日期时间格式化器。
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = formatter(DATE_TIME_PATTERN);

    /**
     * 紧凑日期时间格式化器。
     */
    public static final DateTimeFormatter COMPACT_DATE_TIME_FORMATTER = formatter(COMPACT_DATE_TIME_PATTERN);

    /**
     * 带毫秒的日期时间格式化器。
     */
    public static final DateTimeFormatter MILLIS_DATE_TIME_FORMATTER = formatter(MILLIS_DATE_TIME_PATTERN);

    /**
     * 紧凑日期格式化器。
     */
    public static final DateTimeFormatter COMPACT_DATE_FORMATTER = formatter(COMPACT_DATE_PATTERN);

    /**
     * 紧凑时间格式化器。
     */
    public static final DateTimeFormatter COMPACT_TIME_FORMATTER = formatter(COMPACT_TIME_PATTERN);

    /**
     * 斜杠日期格式化器。
     */
    public static final DateTimeFormatter SLASH_DATE_FORMATTER = formatter(SLASH_DATE_PATTERN);

    /**
     * 年月格式化器。
     */
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER = formatter(YEAR_MONTH_PATTERN);

    /**
     * 年份格式化器。
     */
    public static final DateTimeFormatter YEAR_FORMATTER = formatter(YEAR_PATTERN);

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private static final LocalDate DEFAULT_DATE_FOR_TIME_ONLY = LocalDate.of(1970, 1, 1);

    private static final List<String> AUTO_PARSE_PATTERNS = List.of(
            DATE_TIME_PATTERN,
            DATE_PATTERN,
            MILLIS_DATE_TIME_PATTERN,
            SLASH_DATE_PATTERN,
            COMPACT_DATE_TIME_PATTERN,
            COMPACT_DATE_PATTERN,
            COMPACT_DATE_TIME_WITH_SPACE_PATTERN,
            YEAR_MONTH_PATTERN,
            YEAR_PATTERN,
            TIME_PATTERN
    );

    /**
     * 获取当前时间点。
     *
     * @return 当前时间点。
     */
    public static Instant now() {
        return now(Clock.system(DEFAULT_ZONE));
    }

    /**
     * 使用指定时钟获取当前时间点。
     *
     * @param clock 时钟对象。
     * @return 当前时间点。
     */
    public static Instant now(Clock clock) {
        return Instant.now(Objects.requireNonNull(clock, "clock must not be null"));
    }

    /**
     * 获取当前本地日期。
     *
     * @return 当前日期。
     */
    public static LocalDate today() {
        return today(Clock.system(DEFAULT_ZONE));
    }

    /**
     * 使用指定时钟获取当前本地日期。
     *
     * @param clock 时钟对象。
     * @return 当前日期。
     */
    public static LocalDate today(Clock clock) {
        return LocalDate.now(Objects.requireNonNull(clock, "clock must not be null"));
    }

    /**
     * 获取当前本地日期时间。
     *
     * @return 当前本地日期时间。
     */
    public static LocalDateTime currentDateTime() {
        return currentDateTime(Clock.system(DEFAULT_ZONE));
    }

    /**
     * 使用指定时钟获取当前本地日期时间。
     *
     * @param clock 时钟对象。
     * @return 当前本地日期时间。
     */
    public static LocalDateTime currentDateTime(Clock clock) {
        return LocalDateTime.now(Objects.requireNonNull(clock, "clock must not be null"));
    }

    /**
     * 按指定格式格式化时间对象。
     *
     * @param temporal 时间对象。
     * @param pattern  格式模式。
     * @return 格式化后的字符串。
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        return format(temporal, pattern, DEFAULT_ZONE);
    }

    /**
     * 按指定格式和时区格式化时间对象。
     *
     * @param temporal 时间对象。
     * @param pattern  格式模式。
     * @param zoneId   时区。
     * @return 格式化后的字符串。
     */
    public static String format(TemporalAccessor temporal, String pattern, ZoneId zoneId) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        return formatter(pattern)
                .withZone(resolveZone(zoneId))
                .format(temporal);
    }

    /**
     * 按指定格式格式化旧版 Date 对象。
     *
     * @param date    日期对象。
     * @param pattern 格式模式。
     * @return 格式化后的字符串。
     */
    public static String format(Date date, String pattern) {
        return format(date, pattern, DEFAULT_ZONE);
    }

    /**
     * 按指定格式和时区格式化旧版 Date 对象。
     *
     * @param date    日期对象。
     * @param pattern 格式模式。
     * @param zoneId  时区。
     * @return 格式化后的字符串。
     */
    public static String format(Date date, String pattern, ZoneId zoneId) {
        Objects.requireNonNull(date, "date must not be null");
        return format(date.toInstant(), pattern, zoneId);
    }

    /**
     * 按指定格式解析为本地日期。
     *
     * @param text    日期文本。
     * @param pattern 格式模式。
     * @return 解析后的本地日期。
     */
    public static LocalDate parseLocalDate(String text, String pattern) {
        return LocalDate.parse(requireHasText(text, "text"), formatter(pattern));
    }

    /**
     * 按指定格式解析为本地时间。
     *
     * @param text    时间文本。
     * @param pattern 格式模式。
     * @return 解析后的本地时间。
     */
    public static LocalTime parseLocalTime(String text, String pattern) {
        return LocalTime.parse(requireHasText(text, "text"), formatter(pattern));
    }

    /**
     * 按指定格式解析为本地日期时间。
     *
     * @param text    日期时间文本。
     * @param pattern 格式模式。
     * @return 解析后的本地日期时间。
     */
    public static LocalDateTime parseLocalDateTime(String text, String pattern) {
        return LocalDateTime.parse(requireHasText(text, "text"), formatter(pattern));
    }

    /**
     * 按指定格式解析为 YearMonth。
     *
     * @param text    年月文本。
     * @param pattern 格式模式。
     * @return 解析后的 YearMonth。
     */
    public static YearMonth parseYearMonth(String text, String pattern) {
        return YearMonth.parse(requireHasText(text, "text"), formatter(pattern));
    }

    /**
     * 按指定格式解析为 Year。
     *
     * @param text    年份文本。
     * @param pattern 格式模式。
     * @return 解析后的 Year。
     */
    public static Year parseYear(String text, String pattern) {
        return Year.parse(requireHasText(text, "text"), formatter(pattern));
    }

    /**
     * 按指定格式解析为 Instant。
     *
     * @param text    日期文本。
     * @param pattern 格式模式。
     * @param zoneId  时区。
     * @return 解析后的 Instant。
     */
    public static Instant parseInstant(String text, String pattern, ZoneId zoneId) {
        return parseToInstant(requireHasText(text, "text"), pattern, resolveZone(zoneId));
    }

    /**
     * 按指定格式解析为 Date。
     *
     * @param text    日期文本。
     * @param pattern 格式模式。
     * @return 解析后的 Date。
     */
    public static Date parse(String text, String pattern) {
        return Date.from(parseInstant(text, pattern, DEFAULT_ZONE));
    }

    /**
     * 自动识别常见格式并解析为 Date。
     *
     * @param text 日期文本。
     * @return 解析后的 Date。
     */
    public static Date parse(String text) {
        var source = requireHasText(text, "text");
        for (var pattern : AUTO_PARSE_PATTERNS) {
            if (source.length() != pattern.length()) {
                continue;
            }
            try {
                return parse(source, pattern);
            } catch (IllegalArgumentException ignored) {
                // 尝试下一个候选格式。
            }
        }
        throw new IllegalArgumentException("Unsupported date text: " + source);
    }

    /**
     * 转换日期字符串格式。
     *
     * @param text          原始日期文本。
     * @param sourcePattern 源格式模式。
     * @param targetPattern 目标格式模式。
     * @return 转换后的日期文本。
     */
    public static String convertFormat(String text, String sourcePattern, String targetPattern) {
        return format(parseInstant(text, sourcePattern, DEFAULT_ZONE), targetPattern);
    }

    /**
     * 获取偏移指定天数后的紧凑日期时间字符串。
     *
     * @param dateText 基准日期时间文本，允许为空；为空时取当前时间。
     * @param days     偏移天数，正数表示未来，负数表示过去。
     * @return 偏移后的日期时间字符串。
     */
    public static String getOffsetDate(String dateText, int days) {
        var base = isBlank(dateText)
                ? currentDateTime()
                : parseLocalDateTime(dateText, COMPACT_DATE_TIME_PATTERN);
        return format(base.plusDays(days), COMPACT_DATE_TIME_PATTERN);
    }

    /**
     * 获取当前时间戳对象。
     *
     * @return 当前时间戳。
     */
    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(now());
    }

    /**
     * 按指定格式格式化当前日期。
     *
     * @param pattern 格式模式。
     * @return 格式化后的字符串。
     */
    public static String formatCurrentDate(String pattern) {
        return format(today(), pattern);
    }

    /**
     * 按指定格式格式化当前日期时间。
     *
     * @param pattern 格式模式。
     * @return 格式化后的字符串。
     */
    public static String formatCurrentDateTime(String pattern) {
        return format(currentDateTime(), pattern);
    }

    /**
     * 获取今天零点对应的 Date。
     *
     * @return 今天零点的 Date。
     */
    public static Date currentDate() {
        return toDate(startOfDay(today()));
    }

    /**
     * 截断旧版 Date 的时间部分。
     *
     * @param date 日期对象。
     * @return 仅保留日期部分的新 Date。
     */
    public static Date truncateTime(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return toDate(startOfDay(toLocalDate(date)));
    }

    /**
     * 获取当前日期字符串，格式为 uuuuMMdd。
     *
     * @return 当前日期字符串。
     */
    public static String getCurrentDate() {
        return format(today(), COMPACT_DATE_PATTERN);
    }

    /**
     * 获取当前年月字符串，格式为 uuuuMM。
     *
     * @return 当前年月字符串。
     */
    public static String getCurrentMonth() {
        return format(YearMonth.now(DEFAULT_ZONE), YEAR_MONTH_PATTERN);
    }

    /**
     * 获取当前年份字符串，格式为 uuuu。
     *
     * @return 当前年份字符串。
     */
    public static String getCurrentYear() {
        return format(Year.now(DEFAULT_ZONE), YEAR_PATTERN);
    }

    /**
     * 获取当前时间字符串，格式为 HHmmss。
     *
     * @return 当前时间字符串。
     */
    public static String getCurrentTime() {
        return format(LocalTime.now(DEFAULT_ZONE), COMPACT_TIME_PATTERN);
    }

    /**
     * 校验日期文本是否符合指定格式。
     *
     * @param text    日期文本。
     * @param pattern 格式模式。
     * @return 如果格式合法则返回 true。
     */
    public static boolean isValidDate(String text, String pattern) {
        if (isBlank(text) || isBlank(pattern) || text.length() != pattern.length()) {
            return false;
        }
        try {
            parseToInstant(text, pattern, DEFAULT_ZONE);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * 获取距今天指定天数之前的日期字符串。
     *
     * @param days 天数。
     * @return 日期字符串。
     */
    public static String getDateBefore(int days) {
        return getDateBeforeWithFormat(COMPACT_DATE_PATTERN, days);
    }

    /**
     * 获取距今天指定天数之前的日期字符串。
     *
     * @param pattern 输出格式。
     * @param days    天数。
     * @return 日期字符串。
     */
    public static String getDateBeforeWithFormat(String pattern, int days) {
        return format(today().minusDays(days), pattern);
    }

    /**
     * 获取基准日期之前指定天数的日期字符串。
     *
     * @param dateText 基准日期文本，格式为 uuuuMMdd。
     * @param days     天数。
     * @return 日期字符串。
     */
    public static String getDateBefore(String dateText, int days) {
        return format(parseLocalDate(dateText, COMPACT_DATE_PATTERN).minusDays(days), COMPACT_DATE_PATTERN);
    }

    /**
     * 计算两个旧版 Date 之间相差的天数。
     *
     * @param start 开始日期。
     * @param end   结束日期。
     * @return 天数差，保留正负方向。
     */
    public static long daysBetween(Date start, Date end) {
        return daysBetween(toLocalDate(start), toLocalDate(end));
    }

    /**
     * 计算两个本地日期之间相差的天数。
     *
     * @param start 开始日期。
     * @param end   结束日期。
     * @return 天数差，保留正负方向。
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 获取某天的开始时间。
     *
     * @param date 日期。
     * @return 当天 00:00:00 对应的日期时间。
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return Objects.requireNonNull(date, "date must not be null").atStartOfDay();
    }

    /**
     * 获取某天的结束时间。
     *
     * @param date 日期。
     * @return 当天 23:59:59.999999999 对应的日期时间。
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return Objects.requireNonNull(date, "date must not be null").atTime(LocalTime.MAX);
    }

    /**
     * 将 Instant 转换为 Date。
     *
     * @param instant 时间点。
     * @return 转换后的 Date。
     */
    public static Date toDate(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return Date.from(instant);
    }

    /**
     * 将 LocalDateTime 转换为 Date。
     *
     * @param dateTime 本地日期时间。
     * @return 转换后的 Date。
     */
    public static Date toDate(LocalDateTime dateTime) {
        return toDate(dateTime, DEFAULT_ZONE);
    }

    /**
     * 将 LocalDateTime 按指定时区转换为 Date。
     *
     * @param dateTime 本地日期时间。
     * @param zoneId   时区。
     * @return 转换后的 Date。
     */
    public static Date toDate(LocalDateTime dateTime, ZoneId zoneId) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return Date.from(dateTime.atZone(resolveZone(zoneId)).toInstant());
    }

    /**
     * 将 LocalDate 按指定时区转换为 Date。
     *
     * @param date   本地日期。
     * @param zoneId 时区。
     * @return 转换后的 Date。
     */
    public static Date toDate(LocalDate date, ZoneId zoneId) {
        Objects.requireNonNull(date, "date must not be null");
        return toDate(date.atStartOfDay(), zoneId);
    }

    /**
     * 将 Date 转换为 Instant。
     *
     * @param date 日期对象。
     * @return 转换后的 Instant。
     */
    public static Instant toInstant(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.toInstant();
    }

    /**
     * 将 Date 转换为 LocalDateTime。
     *
     * @param date 日期对象。
     * @return 转换后的 LocalDateTime。
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date, DEFAULT_ZONE);
    }

    /**
     * 将 Date 按指定时区转换为 LocalDateTime。
     *
     * @param date   日期对象。
     * @param zoneId 时区。
     * @return 转换后的 LocalDateTime。
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        Objects.requireNonNull(date, "date must not be null");
        return LocalDateTime.ofInstant(date.toInstant(), resolveZone(zoneId));
    }

    /**
     * 将 Date 转换为 LocalDate。
     *
     * @param date 日期对象。
     * @return 转换后的 LocalDate。
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDate(date, DEFAULT_ZONE);
    }

    /**
     * 将 Date 按指定时区转换为 LocalDate。
     *
     * @param date   日期对象。
     * @param zoneId 时区。
     * @return 转换后的 LocalDate。
     */
    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        return toLocalDateTime(date, zoneId).toLocalDate();
    }

    /**
     * 将 Instant 转换为 Timestamp。
     *
     * @param instant 时间点。
     * @return 转换后的 Timestamp。
     */
    public static Timestamp toTimestamp(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return Timestamp.from(instant);
    }

    private static DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseSensitive()
                .appendPattern(requireHasText(pattern, "pattern"))
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT);
    }

    private static Instant parseToInstant(String text, String pattern, ZoneId zoneId) {
        try {
            var parsed = formatter(pattern).parseBest(
                    text,
                    LocalDateTime::from,
                    LocalDate::from,
                    YearMonth::from,
                    Year::from,
                    LocalTime::from
            );
            if (parsed instanceof LocalDateTime dateTime) {
                return dateTime.atZone(zoneId).toInstant();
            }
            if (parsed instanceof LocalDate date) {
                return date.atStartOfDay(zoneId).toInstant();
            }
            if (parsed instanceof YearMonth yearMonth) {
                return yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
            }
            if (parsed instanceof Year year) {
                return year.atDay(1).atStartOfDay(zoneId).toInstant();
            }
            if (parsed instanceof LocalTime time) {
                return DEFAULT_DATE_FOR_TIME_ONLY.atTime(time).atZone(zoneId).toInstant();
            }
            throw new IllegalArgumentException("Unsupported date pattern: " + pattern);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Failed to parse date text: " + text + ", pattern: " + pattern, exception);
        }
    }

    private static ZoneId resolveZone(ZoneId zoneId) {
        return zoneId == null ? DEFAULT_ZONE : zoneId;
    }

    private static String requireHasText(String text, String fieldName) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
