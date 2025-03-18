package com.whim.common.utils;

/**
 * @author jince
 * date: 2025/3/17 21:00
 * description: 路径工具类
 */
public class PathUtil {

    // 定义枚举类型
    public enum SlashType {
        FORWARD_SLASH,  // 正斜杠
        BACK_SLASH      // 反斜杠
    }

    /**
     * 合并路径
     *
     * @param slashType      斜杠类型
     * @param isAbsolutePath 是否是绝对路径
     * @param paths          路径数组
     * @return 合并后的路径
     */
    public static String mergePath(SlashType slashType, boolean isAbsolutePath, String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }

        final char separator = slashType == SlashType.FORWARD_SLASH ? '/' : '\\';
        boolean startsWithSlash = false;
        boolean firstNonEmptyFound = false;
        StringBuilder result = new StringBuilder();

        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }

            // 确定首个非空路径是否以斜杠开头
            if (!firstNonEmptyFound) {
                char firstChar = path.charAt(0);
                startsWithSlash = firstChar == '/' || firstChar == '\\';
                firstNonEmptyFound = true;
            }

            // 去除首尾的斜杠/反斜杠
            int start = 0;
            int end = path.length();
            while (start < end && (path.charAt(start) == '/' || path.charAt(start) == '\\')) {
                start++;
            }
            while (end > start && (path.charAt(end - 1) == '/' || path.charAt(end - 1) == '\\')) {
                end--;
            }

            // 处理中间的分隔符并拼接
            if (start < end) {
                String segment = path.substring(start, end)
                        .replace('/', separator)
                        .replace('\\', separator);
                if (!result.isEmpty()) {
                    result.append(separator);
                }
                result.append(segment);
            }
        }

        // 仅当 isAbsolutePath 为 true 时，确保结果以斜杠开头
        if (isAbsolutePath && !result.isEmpty() && result.charAt(0) != separator) {
            result.insert(0, separator);
        }
        return result.toString();
    }
}
