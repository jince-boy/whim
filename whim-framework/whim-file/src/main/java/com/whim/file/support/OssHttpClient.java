package com.whim.file.support;

import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.aliyun.sdk.service.oss2.transport.HttpClient;
import com.aliyun.sdk.service.oss2.transport.RequestContext;
import com.aliyun.sdk.service.oss2.transport.RequestException;
import com.aliyun.sdk.service.oss2.transport.RequestMessage;
import com.aliyun.sdk.service.oss2.transport.ResponseMessage;
import com.whim.core.utils.RestClientUtils;
import com.whim.file.config.properties.FileStorageProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/23
 * @description OSS 同步 HTTP 传输，复用 Spring RestClient 与 JDK 连接池并限制请求总时长。
 */
public final class OssHttpClient implements HttpClient, AutoCloseable {
    private final java.net.http.HttpClient httpClient;
    private final RestClient restClient;
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 创建独立传输客户端，禁止重定向以保留 SDK 已签名的目标地址。 */
    public OssHttpClient(FileStorageProperties properties) {
        httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build();
        restClient = RestClientUtils.createRestClient(httpClient, properties.getRequestTimeout());
    }

    /** 原样发送 SDK 已签名的 URI 和正文，保留状态码供 SDK 解析错误，下载流由调用方关闭。 */
    @Override
    public ResponseMessage send(RequestMessage message, RequestContext context) {
        try {
            if (closed.get()) {
                throw new IOException("OSS HTTP 客户端已经关闭");
            }
            RestClient.RequestBodySpec request = restClient.method(HttpMethod.valueOf(message.method()))
                    .uri(message.uri())
                    .headers(headers -> message.headers().forEach(headers::set))
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity");
            BinaryData content = message.body();
            if (content != null) {
                Long length = content.getLength();
                if (length != null) {
                    request.contentLength(length);
                }
                request.body(output -> {
                    try (InputStream input = content.toStream()) {
                        input.transferTo(output);
                    }
                });
            }
            return request.exchange((_, response) -> {
                boolean transferred = false;
                try {
                    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    response.getHeaders().forEach((name, values) -> headers.put(name, String.join(",", values)));
                    ResponseMessage.Builder result = ResponseMessage.newBuilder().request(message)
                            .statusCode(response.getStatusCode().value()).headers(headers);
                    if (response.getStatusCode().is2xxSuccessful()
                            && context.get(RequestContext.Key.HTTP_COMPLETION_OPTION)
                            == RequestContext.HttpCompletionOption.ResponseHeadersRead) {
                        InputStream input = new FilterInputStream(response.getBody()) {
                            /** 释放 Spring 响应及其底层连接，并取消响应流的超时计时。 */
                            @Override
                            public void close() {
                                response.close();
                            }
                        };
                        ResponseMessage download = result.body(BinaryData.fromStream(input)).build();
                        transferred = true;
                        return download;
                    }
                    return result.body(BinaryData.fromBytes(response.getBody().readAllBytes())).build();
                } finally {
                    if (!transferred) {
                        response.close();
                    }
                }
            }, false);
        } catch (Exception exception) {
            throw new RequestException("OSS HTTP 请求失败", exception);
        }
    }

    /** 返回传输实现名称，供 SDK 的客户端标识使用。 */
    @Override
    public String name() {
        return "SpringRestClient";
    }

    /** 取消在途请求并释放 JDK 连接资源，不等待未消费的下载流。 */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            httpClient.shutdownNow();
        }
    }
}
