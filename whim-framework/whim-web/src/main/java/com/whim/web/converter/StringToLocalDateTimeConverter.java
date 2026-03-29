package com.whim.web.converter;

import com.whim.json.config.properties.DateTimeProperties;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 处理 GET 请求参数中字符串到 LocalDateTime 的转换。
 */
public final class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    private static final String EPOCH_TEXT_PATTERN = "^[+-]?(\\d{10}|\\d{13}|\\d{16}|\\d{19})$";

    private final ZoneId zoneId;
    private final List<DateTimeFormatter> dateTimeInputFormatters;
    private final List<DateTimeFormatter> dateInputFormatters;
    private final String supportedInputFormatsDescription;

    /**
     * 创建字符串到 LocalDateTime 的转换器。
     *
     * @param dateTimeProperties 时间配置属性
     */
    public StringToLocalDateTimeConverter(DateTimeProperties dateTimeProperties) {
        Objects.requireNonNull(dateTimeProperties, "参数[dateTimeProperties]不能为空");
        zoneId = ZoneId.of(dateTimeProperties.getZoneId());
        dateTimeInputFormatters = createFormatters(dateTimeProperties.getDateTimeInputPatterns());
        dateInputFormatters = createFormatters(dateTimeProperties.getDateInputPatterns());
        supportedInputFormatsDescription = buildSupportedInputFormatsDescription(dateTimeProperties);
    }

    @Override
    public LocalDateTime convert(String source) {
        var normalizedText = normalizeText(source);
        if (normalizedText == null) {
            return null;
        }

        var instant = tryParseInstantValue(normalizedText);
        if (instant != null) {
            return LocalDateTime.ofInstant(instant, zoneId);
        }

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

        throw new IllegalArgumentException(
                "不支持的 LocalDateTime 参数格式[%s]，支持格式：%s".formatted(
                        source,
                        supportedInputFormatsDescription
                )
        );
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
     * @param dateTimeProperties 时间配置属性
     * @return 输入格式说明
     */
    private static String buildSupportedInputFormatsDescription(DateTimeProperties dateTimeProperties) {
        return "日期时间格式：%s；日期格式：%s；以及 ISO-8601、10/13/16/19 位时间戳"
                .formatted(
                        String.join("、", dateTimeProperties.getDateTimeInputPatterns()),
                        String.join("、", dateTimeProperties.getDateInputPatterns())
                );
    }

    /**
     * 规范化时间文本。
     *
     * @param text 原始时间文本
     * @return 规范化后的时间文本
     */
    private static String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        var normalizedText = text.trim();
        return normalizedText.isEmpty() ? null : normalizedText;
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
     * 尝试解析为 Instant。
     *
     * @param text 时间文本
     * @return Instant，若无法解析则返回 null
     */
    private Instant tryParseInstantValue(String text) {
        var instant = parseEpochInstant(text);
        if (instant != null) {
            return instant;
        }
        return parseOffsetInstant(text);
    }
}
