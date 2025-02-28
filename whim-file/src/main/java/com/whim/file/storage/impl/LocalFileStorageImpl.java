package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
import com.whim.file.FileOptions;
import com.whim.file.config.FileStorageProperties.LocalStorageProperties;
import com.whim.file.storage.IFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 本地文件存储实现
 */
@Component("local")
@Slf4j
public class LocalFileStorageImpl implements IFileStorage {
    @Override
    public void upload(FileOptions fileOptions) {
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        Path basePath = FileUtil.generateAbsolutePath(localStorageProperties.getBasePath());
        Path storagePath = Paths.get(fileOptions.getStoragePath());
        if (!storagePath.isAbsolute()) {
            storagePath = basePath.resolve(storagePath);
        }
        Path fullUploadPath = basePath.resolve(storagePath);
        try {
            Files.createDirectories(fullUploadPath);
            Path filePath = fullUploadPath.resolve(fileOptions.getFileName()).normalize();
            Files.copy(fileOptions.getFileWrapper().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info(localStorageProperties.toString());
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
    }
}
