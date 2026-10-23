package com.whim.file.support;

import com.whim.core.exception.FileStorageException;
import io.minio.Http;
import io.minio.MinioAsyncClient;
import io.minio.Signer;
import io.minio.Time;
import io.minio.Utils;
import io.minio.credentials.Credentials;
import io.minio.errors.MinioException;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/10/23
 * @description 复用 MinIO 客户端及官方 V4 签名器，保留上传签名的类型和长度约束。
 */
public final class MinioStorageClient extends MinioAsyncClient {
    /** 接管已配置客户端的连接池，后续仅关闭本实例。 */
    public MinioStorageClient(MinioAsyncClient client) {
        super(client);
    }

    /** 在签名前保留全部约束头；URL 编码、地域解析和凭据处理沿用 SDK。 */
    public String getUploadPreSignedUrl(String bucket, String key, Map<String, String> headers,
                                        Map<String, String> parameters, Duration expiration) throws MinioException {
        if (expiration == null || expiration.compareTo(Duration.ofSeconds(1)) < 0
                || expiration.compareTo(Duration.ofDays(7)) > 0 || expiration.getNano() != 0) {
            throw new FileStorageException("签名有效期必须为 1 秒至 7 天的整数秒");
        }
        String region = getRegion(bucket, null).join();
        Credentials credentials = provider.fetch();
        Http.QueryParameters query = new Http.QueryParameters(parameters);
        if (credentials.sessionToken() != null) {
            query.put(Http.Headers.X_AMZ_SECURITY_TOKEN, credentials.sessionToken());
        }
        HttpUrl url = baseUrl.buildUrl(Http.Method.PUT, bucket, key, region, query);
        Request.Builder request = new Request.Builder().url(url)
                .put(RequestBody.create(new byte[0], null));
        headers.forEach(request::header);
        request.header(Http.Headers.HOST, Utils.getHostHeader(url))
                .header(Http.Headers.X_AMZ_DATE, ZonedDateTime.now().format(Time.AMZ_DATE_FORMAT));
        // 此请求仅用于签名，不发送空正文，避免高层 API 移除 Content-Type 和 Content-Length。
        return Signer.presignV4(request.build(), region, credentials.accessKey(), credentials.secretKey(),
                Math.toIntExact(expiration.toSeconds())).toString();
    }
}
