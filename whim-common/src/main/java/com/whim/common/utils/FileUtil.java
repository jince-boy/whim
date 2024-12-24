package com.whim.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jince
 * date: 2024/11/19 21:37
 * description: 文件工具类
 */
public class FileUtil {
    /**
     * 将文件大小转换成可视化格式字符串
     *
     * @param fileSize 文件大小
     * @return 可视化文件大小
     */
    public static String formatFileSize(Long fileSize) {
        if (fileSize < FileUtils.ONE_KB) {
            // 小于 1 KB 显示为字节
            return fileSize + " B";
        } else if (fileSize < FileUtils.ONE_MB) {
            // 小于 1 MB 显示为 KB
            return String.format("%.2f KB", fileSize / (double) FileUtils.ONE_KB);
        } else if (fileSize < FileUtils.ONE_GB) {
            // 小于 1 GB 显示为 MB
            return String.format("%.2f MB", fileSize / (double) FileUtils.ONE_MB);
        } else if (fileSize < FileUtils.ONE_TB) {
            // 小于 1 TB 显示为 GB
            return String.format("%.2f GB", fileSize / (double) FileUtils.ONE_GB);
        } else {
            // 1 TB 或更大显示为 TB
            return String.format("%.2f TB", fileSize / (double) FileUtils.ONE_TB);
        }
    }

    /**
     * 根据操作系统和提供的basePath生成绝对路径
     *
     * @param basePath 要转换为绝对路径的基础路径
     * @return 生成不同系统下的绝对路径
     */
    public static Path generateAbsolutePath(String basePath) {
        return Paths.get(StringUtils.prependIfMissing(basePath, "/")).toAbsolutePath().normalize();
    }
}
