package com.whim.file.storage.impl;


import com.whim.core.exception.FileStorageException;
import com.whim.core.utils.PathUtils;
import com.whim.file.client.MinioFileStorageClientFactory;
import com.whim.file.config.properties.FileStorageProperties.MinioStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * @date 2025/2/23 16:31
 * @description minio文件存储实现
 */
@Slf4j
public class MinioFileStorageImpl implements IFileStorage {
    /**
     * 上传文件
     *
     * @param fileHandler 文件选项
     * @return MetaData
     */
    @Override
    public MetaData upload(FileHandler fileHandler) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            try (IFileWrapper fileWrapper = fileHandler.getFileWrapper()) {
                String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
                InputStream inputStream = fileWrapper.getInputStream();
                fileStorageClientFactory.getClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .contentType(fileHandler.getContentType())
                                .stream(inputStream, -1, 5 * 1024 * 1024)
                                .build()
                );
                // 获取文件信息以获取更多元数据
                StatObjectResponse stat = fileStorageClientFactory.getClient().statObject(
                        StatObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .build()
                );
                // 创建并填充元数据对象
                MetaData metaData = new MetaData();
                metaData.setFileName(fileHandler.getFileName());
                metaData.setStoragePath(path);
                metaData.setFileSize(stat.size());
                metaData.setContentType(stat.contentType());
                return metaData;
            }
        } catch (Exception e) {
            throw new FileStorageException("文件上传失败:" + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     *
     * @param fileHandler 文件选项
     * @return DownloadHandler 文件下载处理器
     */
    @Override
    public DownloadHandler download(FileHandler fileHandler) {
        // 获取文件存储属性，这里是Minio存储系统的配置
        MinioStorageProperties properties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory factory = new MinioFileStorageClientFactory(properties)) {
            // 构造文件的完整路径，确保路径格式使用正斜杠
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, properties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            // 使用Minio客户端获取文件对象
            GetObjectResponse object = factory.getClient()
                    .getObject(
                            GetObjectArgs.builder()
                                    .bucket(properties.getBucket()) // 指定存储桶
                                    .object(path) // 指定文件路径
                                    .build()
                    );
            BufferedInputStream bufferedInputStream = new BufferedInputStream(object);
            // 返回文件内容的输入流
            return DownloadHandler.of(bufferedInputStream, () -> {
                try {
                    bufferedInputStream.close();
                } catch (Exception e) {
                    throw new FileStorageException("资源关闭失败", e);
                }
            });
        } catch (ErrorResponseException e) {
            // 当Minio服务器返回错误响应时，抛出文件不存在的异常
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            // 当发生其他异常时，抛出文件获取失败的异常
            throw new FileStorageException("文件获取失败:" + e.getMessage(), e);
        }
    }

    /**
     * 重写获取文件元数据的方法
     *
     * @param fileHandler 文件处理器，包含文件存储路径和文件名等信息
     * @return MetaData对象，包含文件的元数据信息
     * @throws FileStorageException 如果文件不存在或获取文件元信息失败
     */
    @Override
    public MetaData getFileMetaData(FileHandler fileHandler) {
        // 获取Minio存储配置
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        // 创建Minio文件存储客户端工厂，用于与Minio服务器交互
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            // 构造文件的完整路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            try {
                // 获取文件信息以获取更多元数据
                StatObjectResponse stat = fileStorageClientFactory.getClient().statObject(
                        StatObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .build()
                );
                // 创建并填充元数据对象
                MetaData metaData = new MetaData();
                metaData.setFileName(fileHandler.getFileName());
                metaData.setStoragePath(path);
                metaData.setFileSize(stat.size());
                metaData.setContentType(stat.contentType());
                return metaData;
            } catch (ErrorResponseException e) {
                // 如果文件不存在，抛出自定义异常
                throw new FileStorageException("文件不存在", e);
            } catch (Exception e) {
                // 如果获取文件元信息失败，抛出自定义异常
                throw new FileStorageException("获取文件元信息失败", e);
            }
        }
    }


    /**
     * 删除Minio中的文件
     *
     * @param fileHandler 文件选项
     * @return true 删除成功，false 删除失败
     */
    @Override
    public Boolean deleteFile(FileHandler fileHandler) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH,
                    false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            try {
                fileStorageClientFactory.getClient().statObject(
                        StatObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .build()
                );
                fileStorageClientFactory.getClient().removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .build()
                );
                return true;
            } catch (ErrorResponseException e) {
                throw new FileStorageException("删除文件失败:文件不存在", e);
            } catch (Exception e) {
                throw new FileStorageException("删除文件失败", e);
            }
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param fileHandler 文件选项
     * @return true 文件存在，false 文件不存在
     */
    @Override
    public Boolean exists(FileHandler fileHandler) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            fileStorageClientFactory.getClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(path)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new FileStorageException("查找文件异常", e);
        }
    }

    /**
     * 获取Minio中的文件预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            try {
                fileStorageClientFactory.getClient().statObject(
                        StatObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .build()
                );
                return fileStorageClientFactory.getClient().getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(path)
                                .expiry(expire, timeUnit)
                                .method(Method.GET)
                                .build()
                );
            } catch (ErrorResponseException e) {
                throw new FileStorageException("文件预签名URL获取失败:文件不存在", e);
            } catch (Exception e) {
                throw new FileStorageException("文件预签名URL获取失败:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 获取上传文件预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String uploadFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileHandler.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, storageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            return fileStorageClientFactory.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .expiry(expire, timeUnit)
                            .object(path)
                            .method(Method.PUT)
                            .build()
            );
        } catch (Exception e) {
            throw new FileStorageException("获取上传文件预签名url失败", e);
        }
    }
}
