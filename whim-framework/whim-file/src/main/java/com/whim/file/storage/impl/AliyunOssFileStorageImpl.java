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
import com.aliyun.sdk.service.oss2.models.AbortMultipartUploadRequest;
import com.aliyun.sdk.service.oss2.models.CompleteMultipartUploadRequest;
import com.aliyun.sdk.service.oss2.models.CompleteMultipartUpload;
import com.aliyun.sdk.service.oss2.models.InitiateMultipartUploadRequest;
import com.aliyun.sdk.service.oss2.models.ListPartsRequest;
import com.aliyun.sdk.service.oss2.models.ListPartsResult;
import com.aliyun.sdk.service.oss2.models.Part;
import com.aliyun.sdk.service.oss2.models.UploadPartRequest;
import com.whim.file.model.PartUploadSignature;
import com.whim.file.model.StoragePart;
import com.whim.file.storage.MultipartFileStorage;
import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.config.properties.FileStorageProperties.CloudStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.exception.MissingUploadSessionException;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 阿里云 OSS 存储实例，复用客户端并使用 V4 签名。
 */
public final class AliyunOssFileStorageImpl extends BaseFileStorage implements MultipartFileStorage {
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
                .additionalHeaders(List.of("content-length"))
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

    /** 创建 OSS 原生分片会话。 */
    @Override
    public String initiateUpload(String key, String contentType, String taskId) {
        try {
            return client.initiateMultipartUpload(InitiateMultipartUploadRequest.newBuilder().bucket(bucket)
                    .key(objectName(key)).contentType(contentType).build()).uploadId();
        } catch (Exception exception) {
            throw new FileStorageException("创建 OSS 分片任务失败", exception);
        }
    }

    /** 有界分页读取已上传分片。 */
    @Override
    public List<StoragePart> getUploadedParts(String key, String uploadId) {
        try {
            List<StoragePart> parts = new ArrayList<>();
            long marker = 0;
            for (int page = 0; page < 10; page++) {
                ListPartsResult result = client.listParts(ListPartsRequest.newBuilder().bucket(bucket)
                        .key(objectName(key)).uploadId(uploadId).maxParts(1000L).partNumberMarker(marker).build());
                for (Part part : result.parts()) {
                    parts.add(new StoragePart(Math.toIntExact(part.partNumber()), part.size(), part.eTag()));
                }
                if (!Boolean.TRUE.equals(result.isTruncated())) {
                    return parts;
                }
                Long next = result.nextPartNumberMarker();
                if (next == null || next <= marker) {
                    throw new FileStorageException("OSS 分片分页游标无效");
                }
                marker = next;
            }
            throw new FileStorageException("OSS 分片数量超过一万");
        } catch (Exception exception) {
            ServiceException cause = ServiceException.asCause(exception);
            if (cause != null && "NoSuchUpload".equals(cause.errorCode())) {
                throw new MissingUploadSessionException(exception);
            }
            throw new FileStorageException("查询 OSS 分片失败", exception);
        }
    }

    /** Content-MD5 参与签名并由 OSS 校验实际分片内容。 */
    @Override
    public PartUploadSignature getPartUploadSignature(String key, String uploadId, int partNumber,
                                                       long size, String contentMd5, String contentSha256,
                                                       Duration expiration) {
        try {
            var signature = client.presign(UploadPartRequest.newBuilder().bucket(bucket).key(objectName(key))
                            .uploadId(uploadId).partNumber((long) partNumber).contentMD5(contentMd5)
                            .contentLength(size).build(),
                    PresignOptions.newBuilder().expiration(expiration).build());
            Map<String, String> headers = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(signature.signedHeaders().orElse(Map.of()));
            headers.put("Content-MD5", contentMd5);
            // Host 由浏览器根据签名 URL 自动发送，不能通过 XMLHttpRequest 设置。
            headers.remove("Host");
            headers.remove("Content-Length");
            return new PartUploadSignature(signature.url(), Map.copyOf(headers));
        } catch (Exception exception) {
            throw new FileStorageException("生成 OSS 分片上传签名失败", exception);
        }
    }

    /** 以服务端读取的 ETag 清单完成合并。 */
    @Override
    public void completeUpload(String key, String uploadId, List<StoragePart> parts, long size, String contentType) {
        try {
            List<Part> manifest = parts.stream().map(part -> Part.newBuilder()
                    .partNumber((long) part.getPartNumber()).eTag(part.getEtag()).build()).toList();
            client.completeMultipartUpload(CompleteMultipartUploadRequest.newBuilder().bucket(bucket)
                    .key(objectName(key)).uploadId(uploadId)
                    .completeMultipartUpload(CompleteMultipartUpload.newBuilder().parts(manifest).build()).build());
        } catch (Exception exception) {
            throw new FileStorageException("合并 OSS 分片失败", exception);
        }
    }

    /** 会话已不存在时幂等成功，鉴权及网络故障仍然抛出。 */
    @Override
    public void abortUpload(String key, String uploadId) {
        try {
            client.abortMultipartUpload(AbortMultipartUploadRequest.newBuilder().bucket(bucket)
                    .key(objectName(key)).uploadId(uploadId).build());
        } catch (Exception exception) {
            ServiceException cause = ServiceException.asCause(exception);
            if (cause == null || !"NoSuchUpload".equals(cause.errorCode())) {
                throw new FileStorageException("终止 OSS 分片任务失败", exception);
            }
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
