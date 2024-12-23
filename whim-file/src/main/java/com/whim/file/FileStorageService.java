package com.whim.file;

import com.whim.file.adapter.IFileAdapter;
import com.whim.file.storage.IFileStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件存储服务
 */

@Slf4j
@Service
@AllArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final Map<String, IFileStorage> allFileStorage;
    private final List<IFileAdapter> allFileAdapter;

    /**
     * 创建文件处理器
     *
     * @param file 文件
     * @return FileHandler 文件处理器
     */
    public FileHandler createFileHandler(Object file) {
        return new FileHandler(fileStorageProperties, allFileStorage, allFileAdapter).warpFile(file);
    }
}
