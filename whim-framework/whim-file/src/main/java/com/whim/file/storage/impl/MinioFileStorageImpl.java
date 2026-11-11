package com.whim.file.storage.impl;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.config.properties.FileStorageProperties.CloudStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.exception.MissingUploadSessionException;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;
import com.whim.file.support.FileUploadSecurity;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioAsyncClient;
import io.minio.AbortMultipartUploadArgs;
import io.minio.CompleteMultipartUploadArgs;
import io.minio.CreateMultipartUploadArgs;
import io.minio.Http;
import io.minio.messages.ListPartsResult;
import io.minio.messages.Part;
import com.whim.file.model.PartUploadSignature;
import com.whim.file.model.StoragePart;
import com.whim.file.storage.MultipartFileStorage;
import com.whim.file.support.MinioListPartsArguments;
import com.whim.file.support.MinioStorageClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.Http.Method;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/22
 * @description MinIO 存储实例，按配置复用客户端及连接池。
 */
public final class MinioFileStorageImpl extends BaseFileStorage implements MultipartFileStorage {
    private final MinioStorageClient client;
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
        MinioAsyncClient.Builder builder = MinioAsyncClient.builder().endpoint(config.getUrl())
                .credentials(config.getAccessKey(), config.getSecretKey()).httpClient(httpClient, true);
        if (config.getRegion() != null && !config.getRegion().isBlank()) {
            builder.region(config.getRegion());
        }
        client = new MinioStorageClient(builder.build());
    }

    /**
     * 将已校验文件存为二进制附件，业务元数据保留实际检测类型。
     *
     * @param source 已校验的文件输入
     * @param key 文件存储标识
     * @param contentType 文件内容类型
     * @return 文件元数据
     */
    @Override
    public MetaData upload(FileSource source, String key, String contentType) {
        FileUploadSecurity.validateMetadata(key, contentType);
        try {
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName(key))
                    .contentType(FileUploadSecurity.DOWNLOAD_CONTENT_TYPE)
                    .headers(Map.of("Content-Disposition", "attachment"))
                    .stream(source.getInputStream(), source.getSize() < 0 ? null : source.getSize(), 5L * 1024 * 1024)
                    .maxRetries(0)
                    .build()).join();
            return createMetaData(key, source.getInputStream().getBytesRead(), source.getContentType());
        } catch (Exception exception) {
            throw new FileStorageException("MinIO 文件上传失败", exception);
        }
    }

    /** 返回持有响应流的下载处理器，共享客户端保持可用。 */
    @Override
    public DownloadHandler download(String key) {
        try {
            return DownloadHandler.of(client.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objectName(key)).build()).join());
        } catch (Exception exception) {
            throw new FileStorageException("MinIO 文件下载失败", exception);
        }
    }

    /** 获取对象元数据。 */
    @Override
    public MetaData getFileMetaData(String key) {
        try {
            StatObjectResponse metadata = client.statObject(StatObjectArgs.builder()
                    .bucket(bucket).object(objectName(key)).build()).join();
            return createMetaData(key, metadata.size(), metadata.contentType());
        } catch (Exception exception) {
            throw new FileStorageException("获取 MinIO 文件信息失败", exception);
        }
    }

    /** 仅对象不存在返回 false，缺失桶、鉴权及其他故障均保留异常。 */
    @Override
    public boolean exists(String key) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName(key)).build()).join();
            return true;
        } catch (CompletionException exception) {
            String code = getErrorCode(exception);
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
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName(key)).build()).join();
        } catch (Exception exception) {
            throw new FileStorageException("删除 MinIO 文件失败", exception);
        }
    }

    /**
     * 生成强制附件下载签名，覆盖历史对象可能保留的不安全类型。
     *
     * @param key 文件存储标识
     * @param expiration 签名有效期
     * @return 包含安全响应参数的签名地址
     */
    @Override
    public String getFilePreSignedUrl(String key, Duration expiration) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket).object(objectName(key)).method(Method.GET)
                    .extraQueryParams(Map.of("response-content-type", FileUploadSecurity.DOWNLOAD_CONTENT_TYPE,
                            "response-content-disposition", "attachment", "response-cache-control", "no-store"))
                    .expiry(Math.toIntExact(expiration.toSeconds())).build());
        } catch (Exception exception) {
            throw new FileStorageException("生成 MinIO 下载签名失败", exception);
        }
    }

    /**
     * 拒绝绕过内容校验的单次直传。
     *
     * @param key 文件存储标识
     * @param contentType 文件内容类型
     * @param expiration 签名有效期
     * @return 本方法始终拒绝签发并抛出异常
     */
    @Override
    public String uploadFilePreSignedUrl(String key, String contentType, Duration expiration) {
        throw new FileStorageException("单次直传无法校验文件内容，请使用普通上传或分片上传");
    }

    /**
     * 创建二进制附件分片会话，合并对象也不能作为网页内联执行。
     *
     * @param key 文件存储标识
     * @param contentType 客户端声明的类型
     * @param taskId 存储任务标识
     * @return 分片上传会话标识
     */
    @Override
    public String initiateUpload(String key, String contentType, String taskId) {
        FileUploadSecurity.validateMetadata(key, contentType);
        try {
            return client.createMultipartUpload(CreateMultipartUploadArgs.builder().bucket(bucket)
                    .object(objectName(key)).headers(new Http.Headers(Map.of("Content-Type", FileUploadSecurity.DOWNLOAD_CONTENT_TYPE,
                            "Content-Disposition", "attachment",
                            "x-amz-checksum-algorithm", "SHA256")))
                    .build()).join().result().uploadId();
        } catch (Exception exception) {
            throw new FileStorageException("创建 MinIO 分片任务失败", exception);
        }
    }

    /** 分页读取真实分片，校验游标推进，避免异常响应导致无限循环。 */
    @Override
    public List<StoragePart> getUploadedParts(String key, String uploadId) {
        try {
            List<StoragePart> parts = new ArrayList<>();
            Integer marker = null;
            for (int page = 0; page < 10; page++) {
                ListPartsResult result = client.listParts(new MinioListPartsArguments(
                        bucket, objectName(key), uploadId, marker)).join().result();
                for (Part part : result.parts()) {
                    parts.add(new StoragePart(part.partNumber(), part.partSize(), part.etag(), part.checksumSHA256()));
                }
                if (!result.isTruncated()) {
                    return parts;
                }
                Integer next = result.nextPartNumberMarker();
                if (next == null || next <= (marker == null ? 0 : marker)) {
                    throw new FileStorageException("MinIO 分片分页游标无效");
                }
                marker = next;
            }
            throw new FileStorageException("MinIO 分片数量超过一万");
        } catch (Exception exception) {
            if ("NoSuchUpload".equals(getErrorCode(exception))) {
                throw new MissingUploadSessionException(exception);
            }
            throw new FileStorageException("查询 MinIO 分片失败", exception);
        }
    }

    /** 将长度与 SHA-256 一并签名，Content-Length 由浏览器按原始 Blob 自动发送。 */
    @Override
    public PartUploadSignature getPartUploadSignature(String key, String uploadId, int partNumber,
                                                       long size, String contentMd5, String contentSha256,
                                                       Duration expiration) {
        try {
            if (partNumber < 1 || partNumber > 10000 || size < 0) {
                throw new FileStorageException("分片序号或大小无效");
            }
            Map<String, String> headers = Map.of("x-amz-checksum-sha256", contentSha256);
            String url = client.getUploadPreSignedUrl(bucket, objectName(key),
                    Map.of("x-amz-checksum-sha256", contentSha256, "Content-Length", Long.toString(size)),
                    Map.of("uploadId", uploadId, "partNumber", Integer.toString(partNumber)), expiration);
            return new PartUploadSignature(url, headers);
        } catch (Exception exception) {
            throw new FileStorageException("生成 MinIO 分片上传签名失败", exception);
        }
    }

    /**
     * 使用存储端清单合并文件并校验实际内容。
     *
     * @param key 文件存储标识
     * @param uploadId 上传会话标识
     * @param parts 已确认的分片清单
     * @param size 预期文件大小
     * @param contentType 客户端声明的内容类型
     * @return 已校验的文件元数据
     */
    @Override
    public MetaData completeUpload(String key, String uploadId, List<StoragePart> parts, long size, String contentType) {
        FileUploadSecurity.validateMetadata(key, contentType);
        try {
            Part[] manifest = parts.stream().map(part -> new Part(part.getPartNumber(), part.getEtag(),
                            null, null, null, null, part.getContentSha256()))
                    .toArray(Part[]::new);
            client.completeMultipartUpload(CompleteMultipartUploadArgs.builder().bucket(bucket)
                    .object(objectName(key)).uploadId(uploadId).parts(manifest).maxRetries(0).build()).join();
        } catch (Exception exception) {
            throw new FileStorageException("合并 MinIO 分片失败", exception);
        }
        return FileUploadSecurity.getValidatedFileMetaData(this, key);
    }

    /** 已终止或已合并的会话视为清理成功，其他错误保留。 */
    @Override
    public void abortUpload(String key, String uploadId) {
        try {
            client.abortMultipartUpload(AbortMultipartUploadArgs.builder().bucket(bucket)
                    .object(objectName(key)).uploadId(uploadId).build()).join();
        } catch (Exception exception) {
            if (!"NoSuchUpload".equals(getErrorCode(exception))) {
                throw new FileStorageException("终止 MinIO 分片任务失败", exception);
            }
        }
    }

    /** 从异步调用的包装中提取明确的 S3 错误码。 */
    private String getErrorCode(Throwable failure) {
        for (int depth = 0; failure != null && depth < 10; depth++, failure = failure.getCause()) {
            if (failure instanceof ErrorResponseException response) {
                return response.errorResponse().code();
            }
        }
        return null;
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
