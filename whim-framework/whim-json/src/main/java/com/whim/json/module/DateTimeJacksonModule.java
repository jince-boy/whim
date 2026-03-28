package com.whim.json.module;

import com.whim.json.config.properties.DateTimeJsonProperties;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/28
 * @description 时间 Jackson 模块，负责注册时间类型的统一序列化与多格式反序列化能力。
 */
public final class DateTimeJacksonModule extends SimpleModule {
    private static final String EPOCH_TEXT_PATTERN = "^[+-]?(\\d{10}|\\d{13}|\\d{16}|\\d{19})$";

    private final ZoneId zoneId;
    private final DateTimeFormatter dateTimeOutputFormatter;
    private final DateTimeFormatter dateOutputFormatter;
    private final DateTimeFormatter timeOutputFormatter;
    private final List<DateTimeFormatter> dateTimeInputFormatters;
    private final List<DateTimeFormatter> dateInputFormatters;
    private final List<DateTimeFormatter> timeInputFormatters;
    private final String supportedInputFormatsDescription;

    /**
     * 创建时间 Jackson 模块。
     *
     * @param dateTimeJsonProperties 时间配置属性
     */
    public DateTimeJacksonModule(DateTimeJsonProperties dateTimeJsonProperties) {
        super("whim-time-jackson-module");
        Objects.requireNonNull(dateTimeJsonProperties, "参数[timeJsonProperties]不能为空");
        zoneId = ZoneId.of(dateTimeJsonProperties.getZoneId());
        dateTimeOutputFormatter = DateTimeFormatter.ofPattern(dateTimeJsonProperties.getDateTimePattern());
        dateOutputFormatter = DateTimeFormatter.ofPattern(dateTimeJsonProperties.getDatePattern());
        timeOutputFormatter = DateTimeFormatter.ofPattern(dateTimeJsonProperties.getTimePattern());
        dateTimeInputFormatters = createFormatters(dateTimeJsonProperties.getDateTimeInputPatterns());
        dateInputFormatters = createFormatters(dateTimeJsonProperties.getDateInputPatterns());
        timeInputFormatters = createFormatters(dateTimeJsonProperties.getTimeInputPatterns());
        supportedInputFormatsDescription = buildSupportedInputFormatsDescription(dateTimeJsonProperties);

        addSerializer(LocalDateTime.class, new LocalDateTimeValueSerializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeValueDeserializer());
        addSerializer(LocalDate.class, new LocalDateValueSerializer());
        addDeserializer(LocalDate.class, new LocalDateValueDeserializer());
        addSerializer(LocalTime.class, new LocalTimeValueSerializer());
        addDeserializer(LocalTime.class, new LocalTimeValueDeserializer());
        addSerializer(Date.class, new DateValueSerializer());
        addDeserializer(Date.class, new DateValueDeserializer());
        addSerializer(Instant.class, new InstantValueSerializer());
        addDeserializer(Instant.class, new InstantValueDeserializer());
        addSerializer(OffsetDateTime.class, new OffsetDateTimeValueSerializer());
        addDeserializer(OffsetDateTime.class, new OffsetDateTimeValueDeserializer());
        addSerializer(ZonedDateTime.class, new ZonedDateTimeValueSerializer());
        addDeserializer(ZonedDateTime.class, new ZonedDateTimeValueDeserializer());
    }

    /**
     * 根据格式列表创建格式化器列表。
     *
     * @param patterns 格式列表
     * @return 格式化器列表
     */
    private static List<DateTimeFormatter> createFormatters(List<String> patterns) {
        Objects.requireNonNull(patterns, "参数[patterns]不能为空");
        return patterns.stream()
                .map(DateTimeFormatter::ofPattern)
                .toList();
    }

    /**
     * 构建支持的输入格式说明。
     *
     * @param dateTimeJsonProperties 时间配置属性
     * @return 输入格式说明
     */
    private static String buildSupportedInputFormatsDescription(DateTimeJsonProperties dateTimeJsonProperties) {
        return "日期时间格式：%s；日期格式：%s；时间格式：%s；以及 ISO-8601、10/13/16/19 位时间戳"
                .formatted(
                        String.join("、", dateTimeJsonProperties.getDateTimeInputPatterns()),
                        String.join("、", dateTimeJsonProperties.getDateInputPatterns()),
                        String.join("、", dateTimeJsonProperties.getTimeInputPatterns())
                );
    }

    /**
     * 规范化时间文本。
     *
     * @param text 原始时间文本
     * @return 规范化后的时间文本
     */
    private static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    /**
     * 将微秒级时间戳转换为 Instant。
     *
     * @param epochMicros 微秒级时间戳
     * @return Instant
     */
    private static Instant fromEpochMicros(long epochMicros) {
        return Instant.ofEpochSecond(
                Math.floorDiv(epochMicros, 1_000_000L),
                Math.floorMod(epochMicros, 1_000_000L) * 1_000L
        );
    }

    /**
     * 将纳秒级时间戳转换为 Instant。
     *
     * @param epochNanos 纳秒级时间戳
     * @return Instant
     */
    private static Instant fromEpochNanos(long epochNanos) {
        return Instant.ofEpochSecond(
                Math.floorDiv(epochNanos, 1_000_000_000L),
                Math.floorMod(epochNanos, 1_000_000_000L)
        );
    }

    /**
     * 解析时间戳文本。
     *
     * @param text 时间文本
     * @return Instant，若不匹配则返回 null
     */
    private static Instant parseEpochInstant(String text) {
        if (!text.matches(EPOCH_TEXT_PATTERN)) {
            return null;
        }
        var epochValue = Long.parseLong(text);
        var absoluteLength = text.startsWith("-") || text.startsWith("+") ? text.length() - 1 : text.length();
        return switch (absoluteLength) {
            case 10 -> Instant.ofEpochSecond(epochValue);
            case 13 -> Instant.ofEpochMilli(epochValue);
            case 16 -> fromEpochMicros(epochValue);
            case 19 -> fromEpochNanos(epochValue);
            default -> null;
        };
    }

    /**
     * 解析带时区信息的时间文本。
     *
     * @param text 时间文本
     * @return Instant，若不匹配则返回 null
     */
    private static Instant parseOffsetInstant(String text) {
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他带时区格式。
        }
        try {
            return ZonedDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他带时区格式。
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他时间格式。
        }
        if (!text.contains(" ")) {
            return null;
        }
        var normalizedText = text.replace(' ', 'T');
        try {
            return OffsetDateTime.parse(normalizedText).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他带时区格式。
        }
        try {
            return ZonedDateTime.parse(normalizedText).toInstant();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * 解析 Instant。
     *
     * @param text 时间文本
     * @return Instant
     */
    private Instant parseInstantValue(String text) {
        var normalizedText = normalizeText(text);
        var instant = parseEpochInstant(normalizedText);
        if (instant != null) {
            return instant;
        }
        instant = parseOffsetInstant(normalizedText);
        if (instant != null) {
            return instant;
        }
        for (var formatter : dateTimeInputFormatters) {
            try {
                var localDateTime = LocalDateTime.parse(normalizedText, formatter);
                return localDateTime.atZone(zoneId).toInstant();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期时间格式。
            }
        }
        for (var formatter : dateInputFormatters) {
            try {
                var localDate = LocalDate.parse(normalizedText, formatter);
                return localDate.atStartOfDay(zoneId).toInstant();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期格式。
            }
        }
        throw createUnsupportedFormatException(text, "Instant");
    }

    /**
     * 解析 LocalDateTime。
     *
     * @param text 时间文本
     * @return LocalDateTime
     */
    private LocalDateTime parseLocalDateTimeValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) {
            return LocalDateTime.ofInstant(instant, zoneId);
        }
        var normalizedText = normalizeText(text);
        for (var formatter : dateTimeInputFormatters) {
            try {
                return LocalDateTime.parse(normalizedText, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期时间格式。
            }
        }
        for (var formatter : dateInputFormatters) {
            try {
                return LocalDate.parse(normalizedText, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期格式。
            }
        }
        throw createUnsupportedFormatException(text, "LocalDateTime");
    }

    /**
     * 解析 LocalDate。
     *
     * @param text 时间文本
     * @return LocalDate
     */
    private LocalDate parseLocalDateValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) {
            return LocalDateTime.ofInstant(instant, zoneId).toLocalDate();
        }
        var normalizedText = normalizeText(text);
        for (var formatter : dateInputFormatters) {
            try {
                return LocalDate.parse(normalizedText, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期格式。
            }
        }
        for (var formatter : dateTimeInputFormatters) {
            try {
                return LocalDateTime.parse(normalizedText, formatter).toLocalDate();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期时间格式。
            }
        }
        throw createUnsupportedFormatException(text, "LocalDate");
    }

    /**
     * 解析 LocalTime。
     *
     * @param text 时间文本
     * @return LocalTime
     */
    private LocalTime parseLocalTimeValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) {
            return LocalDateTime.ofInstant(instant, zoneId).toLocalTime();
        }
        var normalizedText = normalizeText(text);
        for (var formatter : timeInputFormatters) {
            try {
                return LocalTime.parse(normalizedText, formatter);
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他时间格式。
            }
        }
        for (var formatter : dateTimeInputFormatters) {
            try {
                return LocalDateTime.parse(normalizedText, formatter).toLocalTime();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他日期时间格式。
            }
        }
        throw createUnsupportedFormatException(text, "LocalTime");
    }

    /**
     * 解析 Date。
     *
     * @param text 时间文本
     * @return Date
     */
    private Date parseDateValue(String text) {
        return Date.from(parseInstantValue(text));
    }

    /**
     * 解析 OffsetDateTime。
     *
     * @param text 时间文本
     * @return OffsetDateTime
     */
    private OffsetDateTime parseOffsetDateTimeValue(String text) {
        return OffsetDateTime.ofInstant(parseInstantValue(text), zoneId);
    }

    /**
     * 解析 ZonedDateTime。
     *
     * @param text 时间文本
     * @return ZonedDateTime
     */
    private ZonedDateTime parseZonedDateTimeValue(String text) {
        return ZonedDateTime.ofInstant(parseInstantValue(text), zoneId);
    }

    /**
     * 尝试解析 Instant。
     *
     * @param text 时间文本
     * @return Instant，若不匹配则返回 null
     */
    private Instant tryParseInstantValue(String text) {
        var normalizedText = normalizeText(text);
        var epochInstant = parseEpochInstant(normalizedText);
        if (epochInstant != null) {
            return epochInstant;
        }
        return parseOffsetInstant(normalizedText);
    }

    /**
     * 创建不支持格式的异常。
     *
     * @param text 时间文本
     * @param targetTypeName 目标类型名称
     * @return 非法参数异常
     */
    private IllegalArgumentException createUnsupportedFormatException(String text, String targetTypeName) {
        return new IllegalArgumentException(
                "无法将值 [%s] 解析为 %s，支持的输入格式包括：%s。"
                        .formatted(text, targetTypeName, supportedInputFormatsDescription)
        );
    }

    /**
     * 将 Instant 格式化为统一的日期时间文本。
     *
     * @param instant 时间点
     * @return 日期时间文本
     */
    private String formatInstant(Instant instant) {
        return dateTimeOutputFormatter.format(LocalDateTime.ofInstant(instant, zoneId));
    }

    /**
     * 抽象时间序列化器。
     *
     * @param <T> 时间类型
     */
    private abstract static class AbstractTemporalSerializer<T> extends ValueSerializer<T> {

        /**
         * 序列化时间值。
         *
         * @param value 时间值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public final void serialize(T value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            jgen.writeString(writeValue(value));
        }

        /**
         * 生成目标时间值的输出文本。
         *
         * @param value 时间值
         * @return 输出文本
         */
        protected abstract String writeValue(T value);
    }

    /**
     * 抽象时间反序列化器。
     *
     * @param <T> 时间类型
     */
    private abstract static class AbstractTemporalDeserializer<T> extends ValueDeserializer<T> {

        /**
         * 反序列化时间值。
         *
         * @param parser JSON 解析器
         * @param context 反序列化上下文
         * @return 目标时间值
         */
        @Override
        public final T deserialize(JsonParser parser, DeserializationContext context) {
            var text = parser.getString();
            if (text == null || text.isBlank()) {
                return null;
            }
            return readValue(text);
        }

        /**
         * 将输入文本解析为目标时间值。
         *
         * @param text 输入文本
         * @return 目标时间值
         */
        protected abstract T readValue(String text);
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalDateTime 序列化器。
     */
    private final class LocalDateTimeValueSerializer extends AbstractTemporalSerializer<LocalDateTime> {

        /**
         * 将 LocalDateTime 输出为统一日期时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(LocalDateTime value) {
            return dateTimeOutputFormatter.format(value);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalDateTime 反序列化器。
     */
    private final class LocalDateTimeValueDeserializer extends AbstractTemporalDeserializer<LocalDateTime> {

        /**
         * 将输入文本解析为 LocalDateTime。
         *
         * @param text 输入文本
         * @return LocalDateTime
         */
        @Override
        protected LocalDateTime readValue(String text) {
            return parseLocalDateTimeValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalDate 序列化器。
     */
    private final class LocalDateValueSerializer extends AbstractTemporalSerializer<LocalDate> {

        /**
         * 将 LocalDate 输出为统一日期格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(LocalDate value) {
            return dateOutputFormatter.format(value);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalDate 反序列化器。
     */
    private final class LocalDateValueDeserializer extends AbstractTemporalDeserializer<LocalDate> {

        /**
         * 将输入文本解析为 LocalDate。
         *
         * @param text 输入文本
         * @return LocalDate
         */
        @Override
        protected LocalDate readValue(String text) {
            return parseLocalDateValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalTime 序列化器。
     */
    private final class LocalTimeValueSerializer extends AbstractTemporalSerializer<LocalTime> {

        /**
         * 将 LocalTime 输出为统一时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(LocalTime value) {
            return timeOutputFormatter.format(value);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description LocalTime 反序列化器。
     */
    private final class LocalTimeValueDeserializer extends AbstractTemporalDeserializer<LocalTime> {

        /**
         * 将输入文本解析为 LocalTime。
         *
         * @param text 输入文本
         * @return LocalTime
         */
        @Override
        protected LocalTime readValue(String text) {
            return parseLocalTimeValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Date 序列化器。
     */
    private final class DateValueSerializer extends AbstractTemporalSerializer<Date> {

        /**
         * 将 Date 输出为统一日期时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(Date value) {
            return formatInstant(value.toInstant());
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Date 反序列化器。
     */
    private final class DateValueDeserializer extends AbstractTemporalDeserializer<Date> {

        /**
         * 将输入文本解析为 Date。
         *
         * @param text 输入文本
         * @return Date
         */
        @Override
        protected Date readValue(String text) {
            return parseDateValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Instant 序列化器。
     */
    private final class InstantValueSerializer extends AbstractTemporalSerializer<Instant> {

        /**
         * 将 Instant 输出为统一日期时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(Instant value) {
            return formatInstant(value);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Instant 反序列化器。
     */
    private final class InstantValueDeserializer extends AbstractTemporalDeserializer<Instant> {

        /**
         * 将输入文本解析为 Instant。
         *
         * @param text 输入文本
         * @return Instant
         */
        @Override
        protected Instant readValue(String text) {
            return parseInstantValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description OffsetDateTime 序列化器。
     */
    private final class OffsetDateTimeValueSerializer extends AbstractTemporalSerializer<OffsetDateTime> {

        /**
         * 将 OffsetDateTime 输出为统一日期时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(OffsetDateTime value) {
            return formatInstant(value.toInstant());
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description OffsetDateTime 反序列化器。
     */
    private final class OffsetDateTimeValueDeserializer extends AbstractTemporalDeserializer<OffsetDateTime> {

        /**
         * 将输入文本解析为 OffsetDateTime。
         *
         * @param text 输入文本
         * @return OffsetDateTime
         */
        @Override
        protected OffsetDateTime readValue(String text) {
            return parseOffsetDateTimeValue(text);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description ZonedDateTime 序列化器。
     */
    private final class ZonedDateTimeValueSerializer extends AbstractTemporalSerializer<ZonedDateTime> {

        /**
         * 将 ZonedDateTime 输出为统一日期时间格式。
         *
         * @param value 时间值
         * @return 输出文本
         */
        @Override
        protected String writeValue(ZonedDateTime value) {
            return formatInstant(value.toInstant());
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description ZonedDateTime 反序列化器。
     */
    private final class ZonedDateTimeValueDeserializer extends AbstractTemporalDeserializer<ZonedDateTime> {

        /**
         * 将输入文本解析为 ZonedDateTime。
         *
         * @param text 输入文本
         * @return ZonedDateTime
         */
        @Override
        protected ZonedDateTime readValue(String text) {
            return parseZonedDateTimeValue(text);
        }
    }
}
