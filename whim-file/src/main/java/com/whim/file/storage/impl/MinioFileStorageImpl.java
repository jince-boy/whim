package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.file.FileOptions;
import com.whim.file.client.MinioFileStorageClientFactory;
import com.whim.file.config.FileStorageProperties.MinioStorageProperties;
import com.whim.file.storage.IFileStorage;
import io.minio.PutObjectArgs;
import okio.Path;
import org.springframework.stereotype.Component;

/**
 * @author jince
 * date: 2025/2/23 16:31
 * description: minio文件存储实现
 */
@Component("minio")
public class MinioFileStorageImpl implements IFileStorage {
    @Override
    public void upload(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            Path resolve = Path.get(storageProperties.getBasePath()).resolve(fileOptions.getStoragePath()).resolve(fileOptions.getFileName());
            try {
                fileStorageClientFactory.getClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(storageProperties.getBasePath() + fileOptions.getStoragePath() + "/" + fileOptions.getFileName())
                                .stream(fileOptions.getFileWrapper().getInputStream(), fileOptions.getFileWrapper().getFileSize(), -1)
                                .contentType(fileOptions.getFileWrapper().getContentType())
                                .build()
                );
            } catch (Exception e) {
                throw new FileStorageException("minio保存文件发生错误", e);
            }
        }
    }
}
