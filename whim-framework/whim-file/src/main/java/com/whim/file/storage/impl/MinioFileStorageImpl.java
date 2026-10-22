package com.whim.file.storage.impl;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.config.properties.FileStorageProperties.CloudStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.Http.Method;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/22
 * @description MinIO 存储实例，按配置复用客户端及连接池。
 */
public final class MinioFileStorageImpl extends BaseFileStorage {
    private final MinioClient client;
    private final String bucket;
    private final String prefix;
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 创建有明确连接、读写及请求超时的客户端，不在启动时访问远端。 */
    public MinioFileStorageImpl(CloudStorageProperties config, FileStorageProperties properties) {
        super("minio", config.getName());
        bucket = config.getBucket();
        prefix = StoragePath.validatePrefix(config.getBasePath());
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .writeTimeout(properties.getReadTimeout())
                .callTimeout(properties.getRequestTimeout())
                .build();
        MinioClient.Builder builder = MinioClient.builder().endpoint(config.getUrl())
                .credentials(config.getAccessKey(), config.getSecretKey()).httpClient(httpClient, true);
        if (config.getRegion() != null && !config.getRegion().isBlank()) {
            builder.region(config.getRegion());
        }
        client = builder.build();
    }

    /** 上传文件并使用已传输字节数生成元数据，避免额外 HEAD 请求。 */
    @Override
    public MetaData upload(FileSource source, String key, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName(key))
                    .contentType(contentType)
                    .stream(source.getInputStream(), source.getSize() < 0 ? null : source.getSize(), 5L * 1024 * 1024)
                    .maxRetries(0)
                    .build());
            return createMetaData(key, source.getInputStream().getBytesRead(), contentType);
        } catch (Exception exception) {
            throw new FileStorageException("MinIO 文件上传失败", exception);
        }
    }

    /** 返回持有响应流的下载处理器，共享客户端保持可用。 */
    @Override
    public DownloadHandler download(String key) {
        try {
            return DownloadHandler.of(client.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objectName(key)).build()));
        } catch (Exception exception) {
            throw new FileStorageException("MinIO 文件下载失败", exception);
        }
    }

    /** 获取对象元数据。 */
    @Override
    public MetaData getFileMetaData(String key) {
        try {
            StatObjectResponse metadata = client.statObject(StatObjectArgs.builder()
                    .bucket(bucket).object(objectName(key)).build());
            return createMetaData(key, metadata.size(), metadata.contentType());
        } catch (Exception exception) {
            throw new FileStorageException("获取 MinIO 文件信息失败", exception);
        }
    }

    /** 仅对象不存在返回 false，缺失桶、鉴权及其他故障均保留异常。 */
    @Override
    public boolean exists(String key) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName(key)).build());
            return true;
        } catch (ErrorResponseException exception) {
            String code = exception.errorResponse().code();
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
                return false;
            }
            throw new FileStorageException("检查 MinIO 文件失败", exception);
        } catch (Exception exception) {
            throw new FileStorageException("检查 MinIO 文件失败", exception);
        }
    }

    /** 直接执行幂等删除，不发送额外的存在性请求。 */
    @Override
    public void deleteFile(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName(key)).build());
        } catch (Exception exception) {
            throw new FileStorageException("删除 MinIO 文件失败", exception);
        }
    }

    /** 原样返回 SDK 生成的 GET 签名地址，不额外请求对象。 */
    @Override
    public String getFilePreSignedUrl(String key, Duration expiration) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectName(key)).method(Method.GET)
                    .expiry(Math.toIntExact(expiration.toSeconds())).build());
        } catch (Exception exception) {
            throw new FileStorageException("生成 MinIO 下载签名失败", exception);
        }
    }

    /** 生成 PUT 签名，调用方需要发送指定的 Content-Type。 */
    @Override
    public String uploadFilePreSignedUrl(String key, String contentType, Duration expiration) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectName(key)).method(Method.PUT)
                    .extraHeaders(Map.of("Content-Type", contentType))
                    .expiry(Math.toIntExact(expiration.toSeconds())).build());
        } catch (Exception exception) {
            throw new FileStorageException("生成 MinIO 上传签名失败", exception);
        }
    }

    /** 校验相对 key 并添加配置的对象前缀。 */
    private String objectName(String key) {
        return StoragePath.prepend(prefix, StoragePath.validateKey(key));
    }

    /** 应用关闭时释放连接池，重复调用不会再次关闭。 */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                client.close();
            } catch (Exception exception) {
                throw new FileStorageException("关闭 MinIO 客户端失败", exception);
            }
        }
    }
}
