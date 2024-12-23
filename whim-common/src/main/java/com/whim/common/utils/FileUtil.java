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
     * 检测路径是否在windows系统下，并且是有效路径
     *
     * @param basePath 路径
     * @return true是windows系统下并且为有效路径, false不是windows系统
     */
    public static boolean isWindowsAndValidPath(String basePath) {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win") && basePath.matches("^[a-zA-Z]:.*");
    }

    /**
     * 根据操作系统和提供的basePath生成绝对路径
     *
     * @param basePath 要转换为绝对路径的基础路径
     * @return 生成不同系统下的绝对路径
     */
    public static Path generateAbsolutePath(String basePath) {
        if (isWindowsAndValidPath(basePath)) {
            // Windows 系统且 basePath 是绝对路径
            return Paths.get(basePath).toAbsolutePath().normalize();
        } else {
            // 其他操作系统，包括 Linux 和 macOS，在 basePath 前加上 "/" 确保获取的是绝对路径
            return Paths.get(StringUtils.prependIfMissing(basePath, "/")).toAbsolutePath().normalize();
        }
    }
}
