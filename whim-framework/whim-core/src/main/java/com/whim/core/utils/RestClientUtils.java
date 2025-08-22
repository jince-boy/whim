package com.whim.core.utils;

import com.whim.core.exception.HttpException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author jince
 * date: 2025/8/22 14:19
 * description:  REST客户端工具类
 * 提供基于Spring RestClient的HTTP请求简化方法，支持GET、POST、PUT、DELETE等HTTP方法
 * 采用单例模式设计，线程安全，无需实例化即可使用静态方法
 */
public class RestClientUtils {
    @Getter
    @Setter
    private static RestClient restClient = RestClient.create();

    /**
     * 私有构造函数，防止工具类被实例化
     */
    private RestClientUtils() {
    }

    /**
     * 执行GET请求
     */
    public static <T> T get(String url, Class<T> responseType) {
        return get(url, null, null, responseType);
    }

    /**
     * 执行带查询参数的GET请求
     */
    public static <T> T get(String url, Map<String, Object> params, Class<T> responseType) {
        return get(url, params, null, responseType);
    }

    /**
     * 执行带查询参数和请求头的GET请求
     */
    public static <T> T get(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType) {
        return executeWithErrorHandling(() -> restClient.get()
                .uri(uriBuilder -> buildUriWithParams(uriBuilder, url, params))
                .headers(h -> setHeaders(h, headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpException("HTTP error: " + response.getStatusCode() +
                            ", message: " + response.getStatusText());
                })
                .body(responseType));
    }

    /**
     * 执行简单POST请求
     */
    public static <T> T post(String url, Class<T> responseType) {
        return post(url, null, null, responseType);
    }

    /**
     * 执行带请求体的POST请求
     */
    public static <T> T post(String url, Object body, Class<T> responseType) {
        return post(url, body, null, responseType);
    }

    /**
     * 执行带请求体和请求头的POST请求
     */
    public static <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return executeWithErrorHandling(() -> restClient.post()
                .uri(url)
                .headers(h -> setHeaders(h, headers))
                .body(body != null ? body : Map.of())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpException("HTTP error: " + response.getStatusCode() +
                            ", message: " + response.getStatusText());
                })
                .body(responseType));
    }

    /**
     * 执行简单PUT请求
     */
    public static <T> T put(String url, Class<T> responseType) {
        return put(url, null, null, responseType);
    }

    /**
     * 执行带请求体的PUT请求
     */
    public static <T> T put(String url, Object body, Class<T> responseType) {
        return put(url, body, null, responseType);
    }

    /**
     * 执行带请求体和请求头的PUT请求
     */
    public static <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return executeWithErrorHandling(() -> restClient.put()
                .uri(url)
                .headers(h -> setHeaders(h, headers))
                .body(body != null ? body : Map.of())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpException("HTTP error: " + response.getStatusCode() +
                            ", message: " + response.getStatusText());
                })
                .body(responseType));
    }

    /**
     * 执行简单DELETE请求
     */
    public static <T> T delete(String url, Class<T> responseType) {
        return delete(url, null, null, responseType);
    }

    /**
     * 执行带查询参数的DELETE请求
     */
    public static <T> T delete(String url, Map<String, Object> params, Class<T> responseType) {
        return delete(url, params, null, responseType);
    }

    /**
     * 执行带查询参数和请求头的DELETE请求
     */
    public static <T> T delete(String url, Map<String, Object> params, Map<String, String> headers, Class<T> responseType) {
        return executeWithErrorHandling(() -> restClient.delete()
                .uri(uriBuilder -> buildUriWithParams(uriBuilder, url, params))
                .headers(h -> setHeaders(h, headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new HttpException("HTTP error: " + response.getStatusCode() +
                            ", message: " + response.getStatusText());
                })
                .body(responseType));
    }

    /**
     * 构建带查询参数的URI
     */
    private static URI buildUriWithParams(UriBuilder uriBuilder, String url, Map<String, Object> params) {
        UriBuilder builder = uriBuilder.path(url);
        if (params != null && !params.isEmpty()) {
            params.forEach(builder::queryParam);
        }
        return builder.build();
    }

    /**
     * 设置请求头
     */
    private static void setHeaders(org.springframework.http.HttpHeaders headers, Map<String, String> headerMap) {
        if (headerMap != null && !headerMap.isEmpty()) {
            headers.setAll(headerMap);
        }
    }

    /**
     * 统一异常处理
     */
    private static <T> T executeWithErrorHandling(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw (HttpException) e;
        }
    }
}
