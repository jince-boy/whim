package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件处理器
 */
@Slf4j
public class FileHandler {
    private final FileStorageProperties fileStorageProperties;
    private final Map<String, IFileStorage> allFileStorage;
    private final List<IFileAdapter> allFileAdapter;
    /**
     * 文件包装器
     */
    private IFileWrapper wrapper;
    private IFileStorage fileStorage;
    private final FileInfo fileInfo;

    /**
     * 构造函数
     *
     * @param allFileStorage 全部存储方式
     */
    public FileHandler(FileStorageProperties fileStorageProperties, Map<String, IFileStorage> allFileStorage, List<IFileAdapter> allFileAdapter) {
        this.fileStorageProperties = fileStorageProperties;
        this.allFileStorage = allFileStorage;
        this.allFileAdapter = allFileAdapter;
        this.fileStorage = allFileStorage.get(fileStorageProperties.getDefaultStorage());
        this.fileInfo = new FileInfo();
    }

    /**
     * 包装文件
     *
     * @param file 文件
     * @return FileHandler 文件处理器
     */
    public FileHandler warpFile(Object file) {
        if (Objects.isNull(file)) {
            throw new FileStorageException("文件为空");
        }
        for (IFileAdapter adapter : allFileAdapter) {
            if (adapter.isSupport(file)) {
                this.wrapper = adapter.getFileWrapper(file);
                return this;
            }
        }
        throw new FileStorageException("没有找到支持的适配器");
    }

    /**
     * 设置存储路径
     *
     * @param storagePath 存储路径
     * @return FileHandler 文件处理器
     */
    public FileHandler setStoragePath(String storagePath) {
        if (storagePath == null || storagePath.trim().isBlank()) {
            throw new FileStorageException("文件路径不能为空");
        }
        this.fileInfo.setStoragePath(storagePath);
        return this;
    }

    /**
     * 设置存储平台
     *
     * @param storagePlatform 存储平台
     * @return FileHandler 文件处理器
     */
    public FileHandler setStoragePlatform(String storagePlatform) {
        IFileStorage iFileStorage = allFileStorage.get(storagePlatform);
        if (iFileStorage == null) {
            throw new FileStorageException("未找到对应的存储平台");
        }
        this.fileStorage = iFileStorage;
        this.fileInfo.setStoragePlatform(storagePlatform);
        return this;
    }

    /**
     * 设置文件名称
     *
     * @param fileName 文件名称
     * @return FileHandler 文件处理器
     */
    public FileHandler setFileName(String fileName) {
        if (fileName == null || fileName.trim().isBlank()) {
            throw new FileStorageException("文件名称不能为空");
        }
        if (fileName.matches("^[a-zA-Z0-9_-]{1,255}$")) {
            this.fileInfo.setFileName(fileName);
            return this;
        }
        throw new FileStorageException("文件名称格式不正确");
    }

    /**
     * 给FileInfo填充基本信息
     */
    private void fillFileInfo() {
        // 设置基本属性
        this.fileInfo.setOriginalFileName(this.wrapper.getFileName());
//        FileUtils.byteCountToDisplaySize()
        this.fileInfo.setFileSize(FileUtil.formatFileSize(this.wrapper.getFileSize()));
        this.fileInfo.setExtension(this.wrapper.getFileExtension());
        this.fileInfo.setContentType(this.wrapper.getFileContentType());
        this.fileInfo.setStoragePlatform(Objects.requireNonNullElse(this.fileInfo.getStoragePlatform(), this.fileStorageProperties.getDefaultStorage()));
        this.fileInfo.setStoragePath(Objects.requireNonNullElse(this.fileInfo.getStoragePath(), "/"));
        this.fileInfo.setFileName(Objects.requireNonNullElse(this.fileInfo.getFileName(), this.wrapper.getFileName()));
    }

    /**
     * 上传文件
     *
     * @return FileInfo 上传文件信息
     */
    public FileInfo upload() {
        this.fillFileInfo();
        if (this.fileStorage.upload(this.wrapper, this.fileInfo)) {
            log.info(this.fileInfo.toString());
            return this.fileInfo;
        } else {
            return null;
        }
    }
}
