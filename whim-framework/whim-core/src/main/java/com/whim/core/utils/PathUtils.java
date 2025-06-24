package com.whim.core.utils;

/**
 * @author jince
 * date: 2025/3/17 21:00
 * description: 路径工具类
 */
public class PathUtils {

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
    public static String mergePath(SlashType slashType, boolean isAbsolutePath, boolean isDirectory, String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }

        final char separator = slashType == SlashType.FORWARD_SLASH ? '/' : '\\';
        StringBuilder result = new StringBuilder();

        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
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
        // 如果 isDirectory 为 true，并且当前路径不是空的，并且不是以分隔符结尾，则添加分隔符
        if (isDirectory && !result.isEmpty() && result.charAt(result.length() - 1) != separator) {
            result.append(separator);
        }
        return result.toString();
    }
}
