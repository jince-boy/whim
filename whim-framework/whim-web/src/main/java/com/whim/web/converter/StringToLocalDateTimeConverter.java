package com.whim.web.converter;

import com.whim.json.config.properties.DateTimeProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 处理 GET 请求参数中字符串到 LocalDateTime 的转换。
 */
public final class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    private final DateTimeFormatter dateTimeFormatter;
    private final String dateTimePattern;

    /**
     * 创建字符串到 LocalDateTime 的转换器。
     *
     * @param dateTimeProperties 时间配置属性
     */
    public StringToLocalDateTimeConverter(DateTimeProperties dateTimeProperties) {
        Objects.requireNonNull(dateTimeProperties, "参数[dateTimeProperties]不能为空");
        dateTimePattern = dateTimeProperties.getDateTimePattern();
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    /**
     * 按照统一时间格式将字符串转换为 LocalDateTime。
     *
     * @param source 原始时间文本
     * @return 转换后的 LocalDateTime，空白文本返回 null
     */
    @Override
    public LocalDateTime convert(@NonNull String source) {
        var normalizedText = normalizeText(source);
        if (normalizedText == null) {
            return null;
        }

        try {
            return LocalDateTime.parse(normalizedText, dateTimeFormatter);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "不支持的 LocalDateTime 参数格式[%s]，请使用格式[%s]".formatted(source, dateTimePattern),
                    exception
            );
        }
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
}
