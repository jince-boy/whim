package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/3/24
 * @description 值解析工具类，提供常见基础类型的宽松转换能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueParserUtils {

    private static final char FULL_WIDTH_SPACE = '\u3000';
    private static final char HALF_WIDTH_SPACE = ' ';
    private static final int WIDTH_OFFSET = 65248;
    private static final Set<String> TRUE_VALUES = Set.of("true", "1", "yes", "y", "ok", "on");
    private static final Set<String> FALSE_VALUES = Set.of("false", "0", "no", "n", "off");

    /**
     * 转换为字符串。
     *
     * @param value 原值
     * @return 字符串结果
     */
    public static String toString(Object value) {
        return toString(value, null);
    }

    /**
     * 转换为字符串，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 字符串结果
     */
    public static String toString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (value instanceof Byte[] boxedBytes) {
            byte[] bytes = toPrimitiveBytes(boxedBytes);
            return bytes == null ? defaultValue : new String(bytes, StandardCharsets.UTF_8);
        }
        if (value instanceof ByteBuffer buffer) {
            ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
            byte[] bytes = new byte[readOnlyBuffer.remaining()];
            readOnlyBuffer.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return Objects.toString(value, defaultValue);
    }

    /**
     * 转换为字符。
     *
     * @param value 原值
     * @return 字符结果
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * 转换为字符，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 字符结果
     */
    public static Character toChar(Object value, Character defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Character character) {
            return character;
        }
        String text = normalizedText(value);
        return text == null ? defaultValue : text.charAt(0);
    }

    /**
     * 转换为字节。
     *
     * @param value 原值
     * @return 字节结果
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * 转换为字节，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 字节结果
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.byteValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为短整型。
     *
     * @param value 原值
     * @return 短整型结果
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * 转换为短整型，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 短整型结果
     */
    public static Short toShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.shortValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为整型。
     *
     * @param value 原值
     * @return 整型结果
     */
    public static Integer toInteger(Object value) {
        return toInteger(value, null);
    }

    /**
     * 转换为整型，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 整型结果
     */
    public static Integer toInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为长整型。
     *
     * @param value 原值
     * @return 长整型结果
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * 转换为长整型，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 长整型结果
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(text).longValue();
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为浮点型。
     *
     * @param value 原值
     * @return 浮点型结果
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * 转换为浮点型，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 浮点型结果
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为双精度浮点型。
     *
     * @param value 原值
     * @return 双精度结果
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * 转换为双精度浮点型，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 双精度结果
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为布尔值。
     *
     * @param value 原值
     * @return 布尔结果
     */
    public static Boolean toBoolean(Object value) {
        return toBoolean(value, null);
    }

    /**
     * 转换为布尔值，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 布尔结果
     */
    public static Boolean toBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(normalized)) {
            return true;
        }
        if (FALSE_VALUES.contains(normalized)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * 转换为大整数。
     *
     * @param value 原值
     * @return 大整数结果
     */
    public static BigInteger toBigInteger(Object value) {
        return toBigInteger(value, null);
    }

    /**
     * 转换为大整数，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 大整数结果
     */
    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.toBigInteger();
        }
        if (value instanceof Number number) {
            return BigInteger.valueOf(number.longValue());
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return new BigInteger(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为大数字。
     *
     * @param value 原值
     * @return 大数字结果
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * 转换为大数字，失败时返回默认值。
     *
     * @param value 原值
     * @param defaultValue 默认值
     * @return 大数字结果
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof BigInteger bigInteger) {
            return new BigDecimal(bigInteger);
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    /**
     * 转换为枚举。
     *
     * @param enumType 枚举类型
     * @param value 原值
     * @param <E> 枚举类型
     * @return 枚举结果
     */
    public static <E extends Enum<E>> E toEnum(Class<E> enumType, Object value) {
        return toEnum(enumType, value, null);
    }

    /**
     * 转换为枚举，失败时返回默认值。
     *
     * @param enumType 枚举类型
     * @param value 原值
     * @param defaultValue 默认值
     * @param <E> 枚举类型
     * @return 枚举结果
     */
    public static <E extends Enum<E>> E toEnum(Class<E> enumType, Object value, E defaultValue) {
        Objects.requireNonNull(enumType, "参数[enumType]不能为空");
        if (value == null) {
            return defaultValue;
        }
        if (enumType.isInstance(value)) {
            return enumType.cast(value);
        }
        String text = normalizedText(value);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    /**
     * 拆分并转换为整型数组。
     *
     * @param input 原字符串
     * @param delimiterRegex 分隔表达式
     * @return 整型数组
     */
    public static Integer[] splitToIntegerArray(String input, String delimiterRegex) {
        if (input == null || input.isBlank()) {
            return new Integer[0];
        }
        return Arrays.stream(input.split(delimiterRegex))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .map(segment -> toInteger(segment, 0))
                .toArray(Integer[]::new);
    }

    /**
     * 拆分并转换为长整型数组。
     *
     * @param input 原字符串
     * @param delimiterRegex 分隔表达式
     * @return 长整型数组
     */
    public static Long[] splitToLongArray(String input, String delimiterRegex) {
        if (input == null || input.isBlank()) {
            return new Long[0];
        }
        return Arrays.stream(input.split(delimiterRegex))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .map(segment -> toLong(segment, null))
                .toArray(Long[]::new);
    }

    /**
     * 拆分并转换为字符串数组。
     *
     * @param input 原字符串
     * @param delimiterRegex 分隔表达式
     * @return 字符串数组
     */
    public static String[] splitToStringArray(String input, String delimiterRegex) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(input.split(delimiterRegex))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * 转换为全角文本。
     *
     * @param text 原字符串
     * @return 全角文本
     */
    public static String toFullWidth(String text) {
        return toFullWidth(text, null);
    }

    /**
     * 转换为全角文本，可排除指定字符。
     *
     * @param text 原字符串
     * @param excludedCharacters 排除字符集合
     * @return 全角文本
     */
    public static String toFullWidth(String text, Set<Character> excludedCharacters) {
        if (text == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(text.length());
        for (char current : text.toCharArray()) {
            if (excludedCharacters != null && excludedCharacters.contains(current)) {
                builder.append(current);
                continue;
            }
            if (current == HALF_WIDTH_SPACE) {
                builder.append(FULL_WIDTH_SPACE);
                continue;
            }
            if (current >= 33 && current <= 126) {
                builder.append((char) (current + WIDTH_OFFSET));
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }

    /**
     * 转换为半角文本。
     *
     * @param text 原字符串
     * @return 半角文本
     */
    public static String toHalfWidth(String text) {
        return toHalfWidth(text, null);
    }

    /**
     * 转换为半角文本，可排除指定字符。
     *
     * @param text 原字符串
     * @param excludedCharacters 排除字符集合
     * @return 半角文本
     */
    public static String toHalfWidth(String text, Set<Character> excludedCharacters) {
        if (text == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(text.length());
        for (char current : text.toCharArray()) {
            if (excludedCharacters != null && excludedCharacters.contains(current)) {
                builder.append(current);
                continue;
            }
            if (current == FULL_WIDTH_SPACE) {
                builder.append(HALF_WIDTH_SPACE);
                continue;
            }
            if (current >= '\uFF01' && current <= '\uFF5E') {
                builder.append((char) (current - WIDTH_OFFSET));
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }

    /**
     * 规范化字符串内容。
     *
     * @param value 原值
     * @return 规范化后的字符串
     */
    private static String normalizedText(Object value) {
        String text = toString(value, null);
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 包装字节数组转原始字节数组。
     *
     * @param boxedBytes 包装字节数组
     * @return 原始字节数组
     */
    private static byte[] toPrimitiveBytes(Byte[] boxedBytes) {
        byte[] bytes = new byte[boxedBytes.length];
        for (int i = 0; i < boxedBytes.length; i++) {
            if (boxedBytes[i] == null) {
                return null;
            }
            bytes[i] = boxedBytes[i];
        }
        return bytes;
    }
}
