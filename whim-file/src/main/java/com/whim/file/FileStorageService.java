package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.handler.FileHandler;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件存储服务
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final Map<String, IFileStorage> allFileStorage;
    private final List<IFileAdapter> allFileAdapter;

    private IFileWrapper fileWrapper;

    /**
     * 包装文件
     *
     * @param file 文件对象
     * @return FileStorageService
     */
    public FileStorageService wrap(Object file) {
        if (Objects.isNull(file)) {
            throw new FileStorageException("文件不能为空");
        }
        for (IFileAdapter iFileAdapter : allFileAdapter) {
            if (iFileAdapter.isSupport(file)) {
                this.fileWrapper = iFileAdapter.getFileWrapper(file);
                return this;
            }
        }
        throw new FileStorageException("没有找到支持的文件适配器");
    }

    public FileStorageService createFileHandler(Consumer<FileHandler> fileConfig) {
        FileHandler fileHandler = new FileHandler();
        config.accept(fileHandler);
        return this;
    }

}
