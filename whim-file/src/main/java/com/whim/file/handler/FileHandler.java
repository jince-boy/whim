package com.whim.file.handler;

import com.whim.common.exception.FileStorageException;
import lombok.Getter;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * @author jince
 * date: 2024/12/31 16:12
 * description: 文件处理器
 */
@Getter
public class FileHandler {
    private String fileName;
    private String storagePath;
    private String platform;

    /**
     * 设置文件名称
     *
     * @param fileName 文件名称
     * @return FileHandler
     */
    public FileHandler setFileName(String fileName) {
        if (fileName == null || fileName.trim().isBlank()) {
            throw new FileStorageException("文件名称不能为空");
        }
        if (fileName.matches("^[a-zA-Z0-9_-]{1,255}$")) {
            this.fileName = fileName;
            return this;
        }
        throw new FileStorageException("文件名称格式不正确");
    }

    /**
     * 设置存储路径
     *
     * @param storagePath 存储路径
     * @return FileHandler
     */
    public FileHandler setStoragePath(String storagePath) {
        if (storagePath == null || storagePath.trim().isBlank()) {
            throw new FileStorageException("文件路径不能为空");
        }
        try {
            Paths.get(storagePath); // 验证路径格式
        } catch (InvalidPathException e) {
            throw new FileStorageException("存储路径格式无效: " + storagePath, e);
        }
        this.storagePath = storagePath;
        return this;
    }

    /**
     * 设置存储平台
     *
     * @param platform 存储平台
     * @return FileHandler
     */
    public FileHandler setPlatform(String platform) {
        this.platform = platform;
        return this;
    }
}
