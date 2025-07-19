package com.whim.core.utils;

import io.github.linpeilie.Converter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2024/10/4 22:54
 * @description 类型转换工具类
 */
public class ConvertUtils {
    private final static Converter CONVERTER = SpringUtils.getBean(Converter.class);

    /**
     * 将对象转换为指定类型
     *
     * @param source       被转换的对象
     * @param desc         转换后的对象类型
     * @param <T>          转换前的对象类型
     * @param <V>          转换后的对象类型
     * @return 转换后的对象
     */
    public static <T, V> V convert(T source, Class<V> desc) {
        return CONVERTER.convert(source, desc);
    }

    /**
     * 将对象转换为指定类型
     *
     * @param source       被转换的对象
     * @param desc         转换后的对象类型
     * @param <T>          转换前的对象类型
     * @param <V>          转换后的对象类型
     * @return 转换后的对象
     */
    public static <T, V> V convert(T source, V desc) {
        return CONVERTER.convert(source, desc);
    }

    /**
     * 将对象列表转换为指定类型列表
     *
     * @param sourceList 被转换的对象列表
     * @param desc       转换后的对象类型
     * @param <T>        转换前的对象类型
     * @param <V>        转换后的对象类型
     * @return 转换后的对象列表
     */
    public static <T, V> List<V> convert(List<T> sourceList, Class<V> desc) {
        return CONVERTER.convert(sourceList, desc);
    }

    /**
     * 将Map转换为指定对象
     *
     * @param map          被转换的Map
     * @param beanClass    转换后的对象类型
     * @param <T>          转换前的对象类型
     * @return 转换后的对象
     */
    public static <T> T convert(Map<String, Object> map, Class<T> beanClass) {
        return CONVERTER.convert(map, beanClass);
    }


    /**
     * 将对象转换为字符串
     *
     * @param value 被转换的值
     * @return 结果字符串
     */
    public static String toString(Object value) {
        return toString(value, null);
    }

    /**
     * 将对象转换为字符串
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果字符串
     */
    public static String toString(Object value, String defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof byte[]) {
            return new String((byte[]) value); // 使用默认字符集
        } else if (value instanceof Byte[] bytes) {
            byte[] dest = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                dest[i] = bytes[i]; // 自动拆箱
            }
            return new String(dest); // 使用默认字符集
        } else if (value instanceof ByteBuffer byteBuffer) {
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes); // 获取剩余字节
            return new String(bytes); // 使用默认字符集
        }
        return value.toString(); // 对于其他对象，调用默认 toString()
    }

    /**
     * 将对象转换为字符型
     *
     * @param value 被转换的值
     * @return 结果字符
     */
    public static Character toChar(Object value) {
        return toChar(value, null);
    }

    /**
     * 将对象转换为字符型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果字符
     */
    public static Character toChar(Object value, Character defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof Character) {
            return (Character) value;
        }
        String str = toString(value, null);
        return StringUtils.isEmpty(str) ? defaultValue : str.charAt(0);
    }

    /**
     * 将对象转换为Byte类型
     *
     * @param value 被转换的值
     * @return 结果Byte
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * 将对象转换为Byte类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Byte
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Byte.parseByte(valueStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Short类型
     *
     * @param value 被转换的值
     * @return 结果Short
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * 将对象转换为Short类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Short
     */
    public static Short toShort(Object value, Short defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof Short) {
            return (Short) value;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        String str = toString(value, null);
        if (StringUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Number类型
     *
     * @param value 被转换的值
     * @return 结果Number
     */
    public static Number toNumber(Object value) {
        return toNumber(value, null);
    }

    /**
     * 将对象转换为Number类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Number
     */
    public static Number toNumber(Object value, Number defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }
        try {
            return NumberFormat.getInstance().parse(valueStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象转换为Integer类型
     *
     * @param value 被转换的值
     * @return 结果Integer
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    /**
     * 将对象转换为Integer类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Integer
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueStr.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Long类型
     *
     * @param value 被转换的值
     * @return 结果Long
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * 将对象转换为Long类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Long
     */
    public static Long toLong(Object value, Long defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            // 支持科学计数法
            return new BigDecimal(valueStr.trim()).longValue();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Double类型
     *
     * @param value 被转换的值
     * @return 结果Double
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * 将对象转换为Double类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Double
     */
    public static Double toDouble(Object value, Double defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            // 支持科学计数法
            return new BigDecimal(valueStr.trim()).doubleValue();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Float类型
     *
     * @param value 被转换的值
     * @return 结果Float
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * 将对象转换为Float类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Float
     */
    public static Float toFloat(Object value, Float defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(valueStr.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为Boolean类型
     *
     * @param value 被转换的值
     * @return 结果Boolean
     */
    public static Boolean toBoolean(Object value) {
        return toBoolean(value, null);
    }

    /**
     * 将对象转换为Boolean类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果Boolean
     */
    public static Boolean toBoolean(Object value, Boolean defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        valueStr = valueStr.trim().toLowerCase();

        return switch (valueStr) {
            case "true", "yes", "ok", "1" -> true;
            case "false", "no", "0" -> false;
            default -> defaultValue;
        };
    }

    /**
     * 将字符串转换为Integer数组
     *
     * @param split 分隔符
     * @param str   待转换的字符串
     * @return 结果Integer数组
     */
    public static Integer[] toIntArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Integer[0]; // 使用新语法创建空数组
        }
        return Arrays.stream(str.split(split)) // 将字符串分割为流
                .map(s -> toInt(s, 0)) // 转换每个字符串为整数
                .toArray(Integer[]::new); // 收集结果为 Integer 数组
    }

    /**
     * 将字符串转换为Long数组
     *
     * @param split 分隔符
     * @param str   待转换的字符串
     * @return 结果Long数组
     */
    public static Long[] toLongArray(String split, String str) {
        if (StringUtils.isEmpty(str)) {
            return new Long[0]; // 使用新语法创建空数组
        }

        return Arrays.stream(str.split(split)) // 将字符串分割为流
                .map(s -> toLong(s, null)) // 转换每个字符串为 Long
                .toArray(Long[]::new); // 收集结果为 Long 数组
    }

    /**
     * 将字符串转换为String数组
     *
     * @param split 分隔符
     * @param str   待转换的字符串
     * @return 结果String数组
     */
    public static String[] toStringArray(String split, String str) {
        return str.split(split);
    }

    /**
     * 将对象转换为枚举类型
     *
     * @param clazz 目标枚举类型
     * @param value 被转换的值
     * @param <E>   枚举类型
     * @return 结果枚举类型
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value) {
        return toEnum(clazz, value, null);
    }

    /**
     * 将对象转换为枚举类型
     *
     * @param clazz        目标枚举类型
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @param <E>          枚举类型
     * @return 结果枚举类型
     */
    public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (clazz.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            E enumValue = (E) value; // 强制类型转换
            return enumValue;
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(clazz, valueStr.trim().toUpperCase()); // 将字符串转换为大写以匹配枚举常量
        } catch (IllegalArgumentException e) {
            return defaultValue; // 捕获非法参数异常
        }
    }

    /**
     * 转换为BigInteger类型
     *
     * @param value 被转换的值
     * @return 结果BigInteger
     */
    public static BigInteger toBigInteger(Object value) {
        return toBigInteger(value, null);
    }

    /**
     * 转换为BigInteger类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果BigInteger
     */
    public static BigInteger toBigInteger(Object value, BigInteger defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Long) {
            return BigInteger.valueOf((Long) value);
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigInteger(valueStr.trim()); // 去除前后空格
        } catch (NumberFormatException e) {
            return defaultValue; // 捕获数字格式异常
        }
    }

    /**
     * 转换为BigDecimal类型
     *
     * @param value 被转换的值
     * @return 结果BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * 转换为BigDecimal类型
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue()); // 统一处理所有数字类型
        }

        String valueStr = toString(value, null);
        if (StringUtils.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr.trim()); // 去除前后空格
        } catch (NumberFormatException e) {
            return defaultValue; // 捕获数字格式异常
        }
    }

    /**
     * 将字符串转换为全角字符
     *
     * @param input 被转换的字符串
     * @return 结果字符串
     */

    public static String toSBC(String input) {
        return toSBC(input, null);
    }

    /**
     * 将字符串转换为全角字符
     *
     * @param text          被转换的字符串
     * @param notConvertSet 不替换的字符集合
     * @return 结果字符串
     */
    public static String toSBC(String text, Set<Character> notConvertSet) {
        if (Objects.isNull(text)) {
            return null; // 处理输入为 null 的情况
        }
        StringBuilder sb = new StringBuilder(text.length()); // 使用 StringBuilder 提高效率
        for (char c : text.toCharArray()) {
            if (notConvertSet != null && notConvertSet.contains(c)) {
                sb.append(c); // 跳过不替换的字符
                continue;
            }
            if (c == ' ') {
                sb.append('\u3000'); // 替换空格为全角空格
            } else if (c < '\u007F') {
                sb.append((char) (c + 65248)); // 转换为全角字符
            } else {
                sb.append(c); // 直接添加其他字符
            }
        }
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 将字符串转换为半角字符
     *
     * @param text 被转换的字符串
     * @return 结果字符串
     */
    public static String toDBC(String text) {
        return toDBC(text, null);
    }

    /**
     * 将字符串转换为半角字符
     *
     * @param text          被转换的字符串
     * @param notConvertSet 不替换的字符集合
     * @return 结果字符串
     */
    public static String toDBC(String text, Set<Character> notConvertSet) {
        if (text == null) {
            return null; // 处理输入为 null 的情况
        }

        StringBuilder sb = new StringBuilder(text.length()); // 使用 StringBuilder 提高效率
        for (char c : text.toCharArray()) {
            if (notConvertSet != null && notConvertSet.contains(c)) {
                sb.append(c); // 跳过不替换的字符
                continue;
            }

            if (c == '\u3000') {
                sb.append(' '); // 替换全角空格为半角空格
            } else if (c > '\uFF00' && c < '｟') {
                sb.append((char) (c - 65248)); // 转换全角字符为半角字符
            } else {
                sb.append(c); // 直接添加其他字符
            }
        }
        return sb.toString(); // 返回结果字符串
    }

    /**
     * 将数字转换为大写金额
     *
     * @param n 待转换的数字
     * @return 结果大写金额
     */
    public static String digitUppercase(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        // 处理负数
        String head = n < 0 ? "负" : "";
        n = Math.abs(n);

        StringBuilder result = new StringBuilder();

        // 处理小数部分
        for (int i = 0; i < fraction.length; i++) {
            int fractionalPart = (int) (n * 10 * Math.pow(10, i)) % 10;
            if (fractionalPart != 0) {
                result.append(digit[fractionalPart]).append(fraction[i]);
            }
        }

        // 如果小数部分为空，则表示为整
        if (result.isEmpty()) {
            result.append("整");
        }

        // 处理整数部分
        int integerPart = (int) Math.floor(n);
        if (integerPart == 0) {
            return head + result.toString().replaceAll("(零.)+", "").replaceFirst("(零.)+", "").replaceAll("^整$", "零元整");
        }

        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            StringBuilder p = new StringBuilder();
            for (int j = 0; j < unit[1].length && integerPart > 0; j++) {
                int currentDigit = integerPart % 10;
                if (currentDigit != 0) {
                    p.insert(0, digit[currentDigit] + unit[1][j]);
                }
                integerPart /= 10;
            }
            result.insert(0, p.toString() + unit[0][i]);
        }

        // 清理结果中的多余零
        return head + result.toString()
                .replaceAll("(零.)*零元", "元") // 去除多余的零元
                .replaceFirst("(零.)+", "") // 去掉开头的零
                .replaceAll("(零.)+", "零") // 将多个零替换为一个零
                .replaceAll("^整$", "零元整"); // 特殊情况处理
    }
}
