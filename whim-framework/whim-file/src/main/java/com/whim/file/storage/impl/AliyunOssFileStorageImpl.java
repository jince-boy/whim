package com.whim.file.storage.impl;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.exceptions.ServiceException;
import com.aliyun.sdk.service.oss2.models.DeleteObjectRequest;
import com.aliyun.sdk.service.oss2.models.GetObjectRequest;
import com.aliyun.sdk.service.oss2.models.HeadObjectRequest;
import com.aliyun.sdk.service.oss2.models.HeadObjectResult;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.config.properties.FileStorageProperties.CloudStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 阿里云 OSS 存储实例，复用客户端并使用 V4 签名。
 */
public final class AliyunOssFileStorageImpl extends BaseFileStorage {
    private final OSSClient client;
    private final String bucket;
    private final String prefix;
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 创建具有连接和读写超时的 OSS V2 客户端，地域必须与桶一致。 */
    public AliyunOssFileStorageImpl(CloudStorageProperties config, FileStorageProperties properties) {
        super("aliyunOss", config.getName());
        bucket = config.getBucket();
        prefix = StoragePath.validatePrefix(config.getBasePath());
        if (config.getRegion() == null || config.getRegion().isBlank()) {
            throw new FileStorageException("阿里云 OSS 配置必须指定地域");
        }
        client = OSSClient.newBuilder().endpoint(config.getUrl()).region(config.getRegion())
                .credentialsProvider(new StaticCredentialsProvider(config.getAccessKey(), config.getSecretKey()))
                .signatureVersion("v4").retryMaxAttempts(1)
                .connectTimeout(properties.getConnectTimeout()).readWriteTimeout(properties.getReadTimeout())
                .build();
    }

    /** 流式上传文件，未知大小仍受实际读取上限约束。 */
    @Override
    public MetaData upload(FileSource source, String key, String contentType) {
        try {
            client.putObject(PutObjectRequest.newBuilder().bucket(bucket).key(objectName(key))
                    .contentType(contentType)
                    .body(BinaryData.fromStream(source.getInputStream(), source.getSize() < 0 ? null : source.getSize()))
                    .build());
            return createMetaData(key, source.getInputStream().getBytesRead(), contentType);
        } catch (Exception exception) {
            throw new FileStorageException("阿里云 OSS 文件上传失败", exception);
        }
    }

    /** 直接获取对象，不先执行存在性查询；关闭响应流会释放本次连接。 */
    @Override
    public DownloadHandler download(String key) {
        try {
            return DownloadHandler.of(client.getObject(GetObjectRequest.newBuilder()
                    .bucket(bucket).key(objectName(key)).build()).body());
        } catch (Exception exception) {
            throw new FileStorageException("阿里云 OSS 文件下载失败", exception);
        }
    }

    /** 获取对象大小和内容类型。 */
    @Override
    public MetaData getFileMetaData(String key) {
        try {
            HeadObjectResult metadata = client.headObject(HeadObjectRequest.newBuilder()
                    .bucket(bucket).key(objectName(key)).build());
            return createMetaData(key, metadata.contentLength(), metadata.contentType());
        } catch (Exception exception) {
            throw new FileStorageException("获取阿里云 OSS 文件信息失败", exception);
        }
    }

    /** 只有 NoSuchKey 视为不存在，其余错误作为存储故障抛出。 */
    @Override
    public boolean exists(String key) {
        try {
            client.headObject(HeadObjectRequest.newBuilder().bucket(bucket).key(objectName(key)).build());
            return true;
        } catch (Exception exception) {
            ServiceException serviceException = ServiceException.asCause(exception);
            if (serviceException != null && "NoSuchKey".equals(serviceException.errorCode())) {
                return false;
            }
            throw new FileStorageException("检查阿里云 OSS 文件失败", exception);
        }
    }

    /** 幂等删除对象。 */
    @Override
    public void deleteFile(String key) {
        try {
            client.deleteObject(DeleteObjectRequest.newBuilder().bucket(bucket).key(objectName(key)).build());
        } catch (Exception exception) {
            throw new FileStorageException("删除阿里云 OSS 文件失败", exception);
        }
    }

    /** 原样返回下载签名，保留路径及查询参数编码。 */
    @Override
    public String getFilePreSignedUrl(String key, Duration expiration) {
        try {
            return client.presign(GetObjectRequest.newBuilder().bucket(bucket).key(objectName(key)).build(),
                    PresignOptions.newBuilder().expiration(expiration).build()).url();
        } catch (Exception exception) {
            throw new FileStorageException("生成阿里云 OSS 下载签名失败", exception);
        }
    }

    /** 原样返回 PUT 签名，调用方必须使用相同的 Content-Type。 */
    @Override
    public String uploadFilePreSignedUrl(String key, String contentType, Duration expiration) {
        try {
            return client.presign(PutObjectRequest.newBuilder().bucket(bucket).key(objectName(key))
                            .contentType(contentType).build(),
                    PresignOptions.newBuilder().expiration(expiration).build()).url();
        } catch (Exception exception) {
            throw new FileStorageException("生成阿里云 OSS 上传签名失败", exception);
        }
    }

    /** 校验相对 key 并添加配置前缀。 */
    private String objectName(String key) {
        return StoragePath.prepend(prefix, StoragePath.validateKey(key));
    }

    /** 应用关闭时释放共享客户端。 */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                client.close();
            } catch (Exception exception) {
                throw new FileStorageException("关闭阿里云 OSS 客户端失败", exception);
            }
        }
    }
}
