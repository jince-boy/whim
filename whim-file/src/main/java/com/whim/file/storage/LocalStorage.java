package com.whim.file.storage;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
import com.whim.file.FileInfo;
import com.whim.file.FileStorageProperties;
import com.whim.file.wrapper.IFileWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Jince
 * date: 2024/11/15 22:11
 * description:
 */
@Slf4j
@Component("local")
@RequiredArgsConstructor
public class LocalStorage implements IFileStorage {
    private final FileStorageProperties fileStorageProperties;

    @Override
    public Boolean upload(IFileWrapper fileWrapper, FileInfo fileInfo) {
        // 生成基础路径
        Path basePath = FileUtil.generateAbsolutePath(this.fileStorageProperties.getLocal().getBasePath());
        // 完整路径
        Path fullUploadPath = basePath.resolve(fileInfo.getStoragePath()).normalize();
        fileInfo.setBasePath(basePath.toString());
        try {
            Files.createDirectories(fullUploadPath);
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
        Path filePath = fullUploadPath.resolve(fileInfo.getFileName() + "." + fileInfo.getExtension()).normalize();
        try {
            Files.copy(fileWrapper.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
        return true;
    }
}
