package com.whim.json.module;

import com.whim.json.config.properties.DateTimeProperties;
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
     * @param dateTimeProperties 时间配置属性
     */
    public DateTimeJacksonModule(DateTimeProperties dateTimeProperties) {
        super("whim-time-jackson-module");
        Objects.requireNonNull(dateTimeProperties, "参数[dateTimeProperties]不能为空");
        zoneId = ZoneId.of(dateTimeProperties.getZoneId());
        dateTimeOutputFormatter = DateTimeFormatter.ofPattern(dateTimeProperties.getDateTimePattern());
        dateOutputFormatter = DateTimeFormatter.ofPattern(dateTimeProperties.getDatePattern());
        timeOutputFormatter = DateTimeFormatter.ofPattern(dateTimeProperties.getTimePattern());
        dateTimeInputFormatters = createFormatters(dateTimeProperties.getDateTimeInputPatterns());
        dateInputFormatters = createFormatters(dateTimeProperties.getDateInputPatterns());
        timeInputFormatters = createFormatters(dateTimeProperties.getTimeInputPatterns());
        supportedInputFormatsDescription = buildSupportedInputFormatsDescription(dateTimeProperties);

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

    private static List<DateTimeFormatter> createFormatters(List<String> patterns) {
        Objects.requireNonNull(patterns, "参数[patterns]不能为空");
        return patterns.stream().map(DateTimeFormatter::ofPattern).toList();
    }

    private static String buildSupportedInputFormatsDescription(DateTimeProperties dateTimeProperties) {
        return "日期时间格式：%s；日期格式：%s；时间格式：%s；以及 ISO-8601、10/13/16/19 位时间戳"
                .formatted(
                        String.join("、", dateTimeProperties.getDateTimeInputPatterns()),
                        String.join("、", dateTimeProperties.getDateInputPatterns()),
                        String.join("、", dateTimeProperties.getTimeInputPatterns())
                );
    }

    private static String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    private static Instant fromEpochMicros(long epochMicros) {
        return Instant.ofEpochSecond(
                Math.floorDiv(epochMicros, 1_000_000L),
                Math.floorMod(epochMicros, 1_000_000L) * 1_000L
        );
    }

    private static Instant fromEpochNanos(long epochNanos) {
        return Instant.ofEpochSecond(
                Math.floorDiv(epochNanos, 1_000_000_000L),
                Math.floorMod(epochNanos, 1_000_000_000L)
        );
    }

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

    private static Instant parseOffsetInstant(String text) {
        try { return OffsetDateTime.parse(text).toInstant(); } catch (DateTimeParseException ignored) {}
        try { return ZonedDateTime.parse(text).toInstant(); } catch (DateTimeParseException ignored) {}
        try { return Instant.parse(text); } catch (DateTimeParseException ignored) {}
        if (!text.contains(" ")) { return null; }
        var normalizedText = text.replace(' ', 'T');
        try { return OffsetDateTime.parse(normalizedText).toInstant(); } catch (DateTimeParseException ignored) {}
        try { return ZonedDateTime.parse(normalizedText).toInstant(); } catch (DateTimeParseException ignored) { return null; }
    }

    private Instant parseInstantValue(String text) {
        var normalizedText = normalizeText(text);
        var instant = parseEpochInstant(normalizedText);
        if (instant != null) { return instant; }
        instant = parseOffsetInstant(normalizedText);
        if (instant != null) { return instant; }
        for (var formatter : dateTimeInputFormatters) {
            try { return LocalDateTime.parse(normalizedText, formatter).atZone(zoneId).toInstant(); } catch (DateTimeParseException ignored) {}
        }
        for (var formatter : dateInputFormatters) {
            try { return LocalDate.parse(normalizedText, formatter).atStartOfDay(zoneId).toInstant(); } catch (DateTimeParseException ignored) {}
        }
        throw createUnsupportedFormatException(text, "Instant");
    }

    private LocalDateTime parseLocalDateTimeValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) { return LocalDateTime.ofInstant(instant, zoneId); }
        var normalizedText = normalizeText(text);
        for (var formatter : dateTimeInputFormatters) {
            try { return LocalDateTime.parse(normalizedText, formatter); } catch (DateTimeParseException ignored) {}
        }
        for (var formatter : dateInputFormatters) {
            try { return LocalDate.parse(normalizedText, formatter).atStartOfDay(); } catch (DateTimeParseException ignored) {}
        }
        throw createUnsupportedFormatException(text, "LocalDateTime");
    }

    private LocalDate parseLocalDateValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) { return LocalDateTime.ofInstant(instant, zoneId).toLocalDate(); }
        var normalizedText = normalizeText(text);
        for (var formatter : dateInputFormatters) {
            try { return LocalDate.parse(normalizedText, formatter); } catch (DateTimeParseException ignored) {}
        }
        for (var formatter : dateTimeInputFormatters) {
            try { return LocalDateTime.parse(normalizedText, formatter).toLocalDate(); } catch (DateTimeParseException ignored) {}
        }
        throw createUnsupportedFormatException(text, "LocalDate");
    }

    private LocalTime parseLocalTimeValue(String text) {
        var instant = tryParseInstantValue(text);
        if (instant != null) { return LocalDateTime.ofInstant(instant, zoneId).toLocalTime(); }
        var normalizedText = normalizeText(text);
        for (var formatter : timeInputFormatters) {
            try { return LocalTime.parse(normalizedText, formatter); } catch (DateTimeParseException ignored) {}
        }
        for (var formatter : dateTimeInputFormatters) {
            try { return LocalDateTime.parse(normalizedText, formatter).toLocalTime(); } catch (DateTimeParseException ignored) {}
        }
        throw createUnsupportedFormatException(text, "LocalTime");
    }

    private Date parseDateValue(String text) {
        return Date.from(parseInstantValue(text));
    }

    private OffsetDateTime parseOffsetDateTimeValue(String text) {
        return OffsetDateTime.ofInstant(parseInstantValue(text), zoneId);
    }

    private ZonedDateTime parseZonedDateTimeValue(String text) {
        return ZonedDateTime.ofInstant(parseInstantValue(text), zoneId);
    }

    private Instant tryParseInstantValue(String text) {
        var normalizedText = normalizeText(text);
        var epochInstant = parseEpochInstant(normalizedText);
        if (epochInstant != null) { return epochInstant; }
        return parseOffsetInstant(normalizedText);
    }

    private IllegalArgumentException createUnsupportedFormatException(String text, String targetTypeName) {
        return new IllegalArgumentException(
                "无法将值 [%s] 解析为 %s，支持的输入格式包括：%s。"
                        .formatted(text, targetTypeName, supportedInputFormatsDescription)
        );
    }

    private String formatInstant(Instant instant) {
        return dateTimeOutputFormatter.format(LocalDateTime.ofInstant(instant, zoneId));
    }

    private abstract static class AbstractTemporalSerializer<T> extends ValueSerializer<T> {
        @Override
        public final void serialize(T value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) { jgen.writeNull(); return; }
            jgen.writeString(writeValue(value));
        }
        protected abstract String writeValue(T value);
    }

    private abstract static class AbstractTemporalDeserializer<T> extends ValueDeserializer<T> {
        @Override
        public final T deserialize(JsonParser parser, DeserializationContext context) {
            var text = parser.getString();
            if (text == null || text.isBlank()) { return null; }
            return readValue(text);
        }
        protected abstract T readValue(String text);
    }

    private final class LocalDateTimeValueSerializer extends AbstractTemporalSerializer<LocalDateTime> {
        @Override protected String writeValue(LocalDateTime value) { return dateTimeOutputFormatter.format(value); }
    }
    private final class LocalDateTimeValueDeserializer extends AbstractTemporalDeserializer<LocalDateTime> {
        @Override protected LocalDateTime readValue(String text) { return parseLocalDateTimeValue(text); }
    }
    private final class LocalDateValueSerializer extends AbstractTemporalSerializer<LocalDate> {
        @Override protected String writeValue(LocalDate value) { return dateOutputFormatter.format(value); }
    }
    private final class LocalDateValueDeserializer extends AbstractTemporalDeserializer<LocalDate> {
        @Override protected LocalDate readValue(String text) { return parseLocalDateValue(text); }
    }
    private final class LocalTimeValueSerializer extends AbstractTemporalSerializer<LocalTime> {
        @Override protected String writeValue(LocalTime value) { return timeOutputFormatter.format(value); }
    }
    private final class LocalTimeValueDeserializer extends AbstractTemporalDeserializer<LocalTime> {
        @Override protected LocalTime readValue(String text) { return parseLocalTimeValue(text); }
    }
    private final class DateValueSerializer extends AbstractTemporalSerializer<Date> {
        @Override protected String writeValue(Date value) { return formatInstant(value.toInstant()); }
    }
    private final class DateValueDeserializer extends AbstractTemporalDeserializer<Date> {
        @Override protected Date readValue(String text) { return parseDateValue(text); }
    }
    private final class InstantValueSerializer extends AbstractTemporalSerializer<Instant> {
        @Override protected String writeValue(Instant value) { return formatInstant(value); }
    }
    private final class InstantValueDeserializer extends AbstractTemporalDeserializer<Instant> {
        @Override protected Instant readValue(String text) { return parseInstantValue(text); }
    }
    private final class OffsetDateTimeValueSerializer extends AbstractTemporalSerializer<OffsetDateTime> {
        @Override protected String writeValue(OffsetDateTime value) { return formatInstant(value.toInstant()); }
    }
    private final class OffsetDateTimeValueDeserializer extends AbstractTemporalDeserializer<OffsetDateTime> {
        @Override protected OffsetDateTime readValue(String text) { return parseOffsetDateTimeValue(text); }
    }
    private final class ZonedDateTimeValueSerializer extends AbstractTemporalSerializer<ZonedDateTime> {
        @Override protected String writeValue(ZonedDateTime value) { return formatInstant(value.toInstant()); }
    }
    private final class ZonedDateTimeValueDeserializer extends AbstractTemporalDeserializer<ZonedDateTime> {
        @Override protected ZonedDateTime readValue(String text) { return parseZonedDateTimeValue(text); }
    }
}
