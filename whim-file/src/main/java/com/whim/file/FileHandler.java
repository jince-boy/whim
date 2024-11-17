package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件处理器
 */
public class FileHandler {
    private final FileStorageProperties fileStorageProperties;
    private final Map<String, IFileStorage> allFileStorage;
    private final List<IFileAdapter> allFileAdapter;
    /**
     * 文件包装器
     */
    private IFileWrapper wrapper;
    private IFileStorage fileStorage;

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

    public FileInfo upload() {
        return new FileInfo();
    }
}
