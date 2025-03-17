package com.whim.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jince
 * date: 2024/11/19 21:37
 * description: 文件工具类
 */
public class FileUtil {

    /**
     * 根据操作系统和提供的basePath生成绝对路径
     *
     * @param basePath 要转换为绝对路径的基础路径
     * @return 生成不同系统下的绝对路径
     */
    public static Path generateAbsolutePath(String basePath) {
        return Paths.get(StringUtils.prependIfMissing(basePath, "/")).toAbsolutePath().normalize();
    }

    /**
     * 拼接多个路径组件为规范化路径字符串
     *
     * @param components 可变数量的路径组件参数（允许null和空白字符串）
     * @return 拼接后的路径字符串（自动处理多余斜杠，保留开头的根斜杠）
     * <p>
     * 实现逻辑：
     * 1. 过滤并预处理有效组件
     * - 忽略null和纯空白组件
     * - 去除组件首尾空白
     * 2. 特殊处理第一个组件
     * - 保留原始开头的斜杠标识
     * - 清理首尾多余斜杠
     * 3. 拼接后续组件
     * - 每个组件去除首尾斜杠后追加
     */
    public static String joinPath(String... components) {
        // 过滤并处理有效组件
        List<String> validComponents = Arrays.stream(components)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (validComponents.isEmpty()) return "";
        boolean isAbsolute = validComponents.getFirst().startsWith("/");
        List<String> processed = validComponents.stream()
                .map(c -> c.replaceAll("^/+", "").replaceAll("/+$", "")) // 去除首尾斜杠
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (processed.isEmpty()) return isAbsolute ? "/" : "";
        String path = String.join("/", processed);
        return isAbsolute ? "/" + path : path;
    }

    public static String joinPath2(boolean useSlash, String... path) {
        return File.separator;
    }
}
