package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
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
    public void upload(FileHandler fileHandler) {
        Path basePath = FileUtil.generateAbsolutePath(fileHandler.getFileStorageProperties().getLocal().getBasePath());
        Path storagePath = Paths.get(fileHandler.getStoragePath());
        if (!storagePath.isAbsolute()) {
            storagePath = basePath.resolve(storagePath);
        }
        Path fullUploadPath = basePath.resolve(storagePath);
        try {
            Files.createDirectories(fullUploadPath);
            Path filePath = fullUploadPath.resolve(fileHandler.getFileName()).normalize();
            Files.copy(fileHandler.getFileWrapper().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info(fileHandler.getFileStorageProperties().toString());
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
    }
}
