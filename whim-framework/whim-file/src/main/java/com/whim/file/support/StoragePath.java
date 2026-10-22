package com.whim.file.support;

import com.whim.core.exception.FileStorageException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 存储相对 key 的跨平台约束，不处理本地绝对根目录。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoragePath {
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
