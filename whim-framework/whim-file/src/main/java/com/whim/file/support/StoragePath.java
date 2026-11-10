package com.whim.file.support;

import com.whim.core.exception.FileStorageException;
import com.whim.core.utils.IdUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 统一存储文件命名、相对对象路径及本地根目录的跨平台约束。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoragePath {
    /**
     * 解析当前操作系统可用的本地根目录，拒绝将其他系统的路径误当成相对目录。
     *
     * @param basePath 本地存储目录
     * @return 当前系统下规范化的绝对目录
     */
    public static Path resolveLocalRoot(String basePath) {
        // Linux 会将 Windows 盘符和反斜杠当作普通字符，必须在创建目录前明确拒绝。
        if (File.separatorChar == '/' && (basePath.indexOf('\\') >= 0 || basePath.matches("^[A-Za-z]:.*"))) {
            throw new FileStorageException("本地存储目录包含Windows路径，请改为当前系统的目录");
        }
        try {
            Path path = Path.of(basePath);
            // Windows 的 /app/files、\\files 和 C:files 均依赖当前盘符，不能作为可迁移的根目录。
            if (path.getRoot() != null && !path.isAbsolute()) {
                throw new FileStorageException("本地存储目录缺少完整根路径，请使用当前系统的绝对目录或普通相对目录");
            }
            return path.toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw new FileStorageException("本地存储目录格式无效", exception);
        }
    }

    /**
     * 从原文件名称提取后缀，不将客户端名称按服务器操作系统的特殊路径规则解析。
     *
     * @param originalName 原始文件名称或路径，可以为空
     * @return 不包含点号的后缀，没有后缀时为空字符串
     */
    public static String getFileExtension(String originalName) {
        if (originalName == null) return "";
        int separator = Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\'));
        int dot = originalName.lastIndexOf('.');
        return dot > separator ? originalName.substring(dot + 1) : "";
    }

    /**
     * 生成两种上传流程共用的安全文件名，原名称仅保留在业务元数据中。
     *
     * @param originalName 原始文件名称或路径，可以为空
     * @return UUID及安全后缀组成的跨平台存储文件名
     */
    public static String generateFileName(String originalName) {
        String extension = getFileExtension(originalName);
        // 限制物理文件名的字符和长度，避免 Linux 特殊名称在 Windows 或迁移后无法使用。
        return IdUtils.uuid32() + (extension.matches("[a-zA-Z0-9]{1,16}") ? "." + extension : ".bin");
    }

    /** 校验非空相对 key，拒绝路径穿越、Windows 特殊路径及歧义名称。 */
    public static String validateKey(String key) {
        if (key == null || key.isBlank() || key.length() > 1024 || key.startsWith("/")
                || key.indexOf('\\') >= 0 || key.chars().anyMatch(Character::isISOControl)) {
            throw new FileStorageException("文件对象路径无效");
        }
        for (String segment : key.split("/", -1)) {
            if (segment.isBlank() || segment.equals(".") || segment.equals("..")
                    || segment.endsWith(".") || segment.endsWith(" ")
                    || segment.matches(".*[:*?\"<>|].*")
                    || segment.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?")) {
                throw new FileStorageException("文件对象路径包含不允许的目录或文件名");
            }
        }
        return key;
    }

    /** 校验对象存储前缀，空字符串表示桶根目录。 */
    public static String validatePrefix(String prefix) {
        return prefix == null || prefix.isEmpty() ? "" : validateKey(prefix);
    }

    /** 在相对 key 前添加对象存储的配置前缀。 */
    public static String prepend(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "/" + key;
    }
}
