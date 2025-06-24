package com.whim.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Jince
 * date: 2024/10/5 00:39
 * description: 字符串格式化工具类
 * <p>
 * 此类提供一种方法来格式化字符串，将占位符 {} 按照顺序替换为参数。
 * <p>
 * 转义规则：
 * - 如果想输出 {} 使用 \ 转义 { 即可
 * - 如果想输出 {} 之前的 \ 使用双转义符 \\ 即可
 * <p>
 * 示例：
 * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b
 * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a
 * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b
 * </p>
 */
public class StringFormatUtils {
    public static final String EMPTY_JSON = "{}";
    public static final char C_BACKSLASH = '\\';
    public static final char C_DELIM_START = '{';

    /**
     * 格式化字符串<br>
     * 此方法将占位符 {} 按照顺序替换为参数<br>
     *
     * @param strPattern 字符串模板
     * @param argArray   参数列表
     * @return 格式化后的字符串
     */
    public static String format(final String strPattern, final Object... argArray) {
        if (StringUtils.isEmpty(strPattern) || Objects.isNull(argArray)) {
            return strPattern;
        }

        final int strPatternLength = strPattern.length();
        StringBuilder stringBuilder = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;
        int delimIndex;

        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = strPattern.indexOf(EMPTY_JSON, handledPosition);
            if (delimIndex == -1) {
                if (handledPosition == 0) {
                    return strPattern;
                } else {
                    stringBuilder.append(strPattern, handledPosition, strPatternLength);
                    return stringBuilder.toString();
                }
            } else {
                if (delimIndex > 0 && strPattern.charAt(delimIndex - 1) == C_BACKSLASH) {
                    if (delimIndex > 1 && strPattern.charAt(delimIndex - 2) == C_BACKSLASH) {
                        stringBuilder.append(strPattern, handledPosition, delimIndex - 1);
                        stringBuilder.append(ConvertUtils.toString(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        argIndex--;
                        stringBuilder.append(strPattern, handledPosition, delimIndex - 1);
                        stringBuilder.append(C_DELIM_START);
                        handledPosition = delimIndex + 1;
                    }
                } else {
                    stringBuilder.append(strPattern, handledPosition, delimIndex);
                    stringBuilder.append(ConvertUtils.toString(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        stringBuilder.append(strPattern, handledPosition, strPatternLength);
        return stringBuilder.toString();
    }
}

