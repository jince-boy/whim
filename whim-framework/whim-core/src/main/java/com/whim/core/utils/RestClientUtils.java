package com.whim.core.utils;

import com.whim.core.exception.HttpException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/26
 * @description RestClient 工具类，提供基于 Spring RestClient 的统一 HTTP 调用能力，支持泛型响应、不可变请求对象与统一异常处理。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestClientUtils {

    /**
     * 默认连接超时时间。
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * 默认读取超时时间。
     */
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 当前使用的 RestClient 实例。
     * -- GETTER --
     *  获取当前 RestClient 实例。
     */
    @Getter
    private static volatile RestClient restClient = createDefaultRestClient();

    /**
     * 替换当前 RestClient 实例。
     *
     * @param restClient RestClient 实例
     */
    public static void setRestClient(RestClient restClient) {
        RestClientUtils.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
    }

    /**
     * 重置为默认 RestClient 实例。
     */
    public static void resetRestClient() {
        restClient = createDefaultRestClient();
    }

    /**
     * 执行请求并按普通类型返回响应体。
     *
     * @param request 请求定义
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T execute(Request request, Class<T> responseType) {
        return executeForEntity(request, responseType).getBody();
    }

    /**
     * 执行请求并按泛型类型返回响应体。
     *
     * @param request 请求定义
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T execute(Request request, ParameterizedTypeReference<T> responseType) {
        return executeForEntity(request, responseType).getBody();
    }

    /**
     * 执行请求并按普通类型返回完整响应。
     *
     * @param request 请求定义
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 完整响应
     */
    public static <T> ResponseEntity<T> executeForEntity(Request request, Class<T> responseType) {
        Objects.requireNonNull(responseType, "responseType must not be null");
        return exchange(request, responseSpec -> responseSpec.toEntity(responseType));
    }

    /**
     * 执行请求并按泛型类型返回完整响应。
     *
     * @param request 请求定义
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 完整响应
     */
    public static <T> ResponseEntity<T> executeForEntity(Request request, ParameterizedTypeReference<T> responseType) {
        Objects.requireNonNull(responseType, "responseType must not be null");
        return exchange(request, responseSpec -> responseSpec.toEntity(responseType));
    }

    /**
     * 执行 GET 请求。
     *
     * @param url 请求地址
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T get(String url, Class<T> responseType) {
        return execute(Request.get(url), responseType);
    }

    /**
     * 执行带查询参数的 GET 请求。
     *
     * @param url 请求地址
     * @param queryParams 查询参数
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T get(String url, Map<String, ?> queryParams, Class<T> responseType) {
        return execute(Request.get(url).withQueryParams(queryParams), responseType);
    }

    /**
     * 执行 POST 请求。
     *
     * @param url 请求地址
     * @param body 请求体
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T post(String url, Object body, Class<T> responseType) {
        return execute(Request.post(url, body), responseType);
    }

    /**
     * 执行 PUT 请求。
     *
     * @param url 请求地址
     * @param body 请求体
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T put(String url, Object body, Class<T> responseType) {
        return execute(Request.put(url, body), responseType);
    }

    /**
     * 执行 PATCH 请求。
     *
     * @param url 请求地址
     * @param body 请求体
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T patch(String url, Object body, Class<T> responseType) {
        return execute(Request.patch(url, body), responseType);
    }

    /**
     * 执行 DELETE 请求。
     *
     * @param url 请求地址
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T delete(String url, Class<T> responseType) {
        return execute(Request.delete(url), responseType);
    }

    /**
     * 执行带查询参数的 DELETE 请求。
     *
     * @param url 请求地址
     * @param queryParams 查询参数
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应体
     */
    public static <T> T delete(String url, Map<String, ?> queryParams, Class<T> responseType) {
        return execute(Request.delete(url).withQueryParams(queryParams), responseType);
    }

    /**
     * 创建默认 RestClient 实例。
     *
     * @return 默认 RestClient 实例
     */
    private static RestClient createDefaultRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(DEFAULT_READ_TIMEOUT);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 执行统一请求交换逻辑。
     *
     * @param request 请求定义
     * @param extractor 响应提取器
     * @param <T> 响应泛型
     * @return 提取结果
     */
    private static <T> T exchange(Request request, ResponseExtractor<T> extractor) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(extractor, "extractor must not be null");

        URI uri = buildUri(request);
        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(request.method()).uri(uri);
            applyHeaders(requestSpec, request);

            if (request.body() != null) {
                requestSpec.body(request.body());
            }

            RestClient.ResponseSpec responseSpec = requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, (clientRequest, clientResponse) -> {
                        throw buildHttpException(request.method(), uri, clientResponse);
                    });
            return extractor.extract(responseSpec);
        } catch (HttpException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HttpException(buildRequestErrorMessage(request.method(), uri), exception);
        }
    }

    /**
     * 应用请求头与媒体类型配置。
     *
     * @param requestSpec 请求规格
     * @param request 请求定义
     */
    private static void applyHeaders(RestClient.RequestBodySpec requestSpec, Request request) {
        if (!request.headers().isEmpty()) {
            requestSpec.headers(headers -> headers.addAll(request.headers()));
        }
        if (request.contentType() != null) {
            requestSpec.contentType(request.contentType());
        }
        if (!request.acceptTypes().isEmpty()) {
            requestSpec.accept(request.acceptTypes().toArray(MediaType[]::new));
        }
    }

    /**
     * 构建最终请求 URI。
     *
     * @param request 请求定义
     * @return 最终 URI
     */
    private static URI buildUri(Request request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.url());
        request.queryParams().forEach((name, value) -> appendQueryParam(builder, name, value));
        return builder.buildAndExpand(request.uriVariables()).encode().toUri();
    }

    /**
     * 追加查询参数。
     *
     * @param builder URI 构建器
     * @param name 参数名
     * @param value 参数值
     */
    private static void appendQueryParam(UriComponentsBuilder builder, String name, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> values) {
            builder.queryParam(name, values.toArray());
            return;
        }
        if (value.getClass().isArray()) {
            builder.queryParam(name, toObjectArray(value));
            return;
        }
        builder.queryParam(name, value);
    }

    /**
     * 将数组转换为对象数组。
     *
     * @param value 原始数组
     * @return 对象数组
     */
    private static Object[] toObjectArray(Object value) {
        if (value instanceof Object[] objectArray) {
            return objectArray;
        }
        if (value instanceof int[] array) {
            return Arrays.stream(array).boxed().toArray();
        }
        if (value instanceof long[] array) {
            return Arrays.stream(array).boxed().toArray();
        }
        if (value instanceof double[] array) {
            return Arrays.stream(array).boxed().toArray();
        }
        if (value instanceof boolean[] array) {
            Object[] result = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i];
            }
            return result;
        }
        if (value instanceof byte[] array) {
            Object[] result = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i];
            }
            return result;
        }
        if (value instanceof short[] array) {
            Object[] result = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i];
            }
            return result;
        }
        if (value instanceof float[] array) {
            Object[] result = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i];
            }
            return result;
        }
        if (value instanceof char[] array) {
            Object[] result = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i];
            }
            return result;
        }
        return new Object[]{value};
    }

    /**
     * 构建 HTTP 异常。
     *
     * @param method 请求方法
     * @param uri 请求地址
     * @param response 客户端响应
     * @return HTTP 异常
     */
    private static HttpException buildHttpException(HttpMethod method, URI uri, org.springframework.http.client.ClientHttpResponse response) {
        try {
            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            String statusText = response.getStatusText();
            return new HttpException(buildResponseErrorMessage(method, uri, response.getStatusCode(), statusText, responseBody));
        } catch (IOException exception) {
            return new HttpException(buildRequestErrorMessage(method, uri), exception);
        }
    }

    /**
     * 构建请求异常消息。
     *
     * @param method 请求方法
     * @param uri 请求地址
     * @return 异常消息
     */
    private static String buildRequestErrorMessage(HttpMethod method, URI uri) {
        return "HTTP request failed, method=%s, uri=%s".formatted(method.name(), uri);
    }

    /**
     * 构建响应异常消息。
     *
     * @param method 请求方法
     * @param uri 请求地址
     * @param statusCode 状态码
     * @param statusText 状态描述
     * @param responseBody 响应体
     * @return 异常消息
     */
    private static String buildResponseErrorMessage(
            HttpMethod method,
            URI uri,
            HttpStatusCode statusCode,
            String statusText,
            String responseBody
    ) {
        String normalizedBody = responseBody == null ? "" : responseBody.trim();
        return "HTTP request failed, method=%s, uri=%s, status=%s, statusText=%s, response=%s"
                .formatted(method.name(), uri, statusCode.value(), statusText, normalizedBody);
    }

    /**
     * 复制并包装为只读请求头。
     *
     * @param headers 原始请求头
     * @return 只读请求头
     */
    private static HttpHeaders copyHeaders(HttpHeaders headers) {
        HttpHeaders copiedHeaders = new HttpHeaders();
        if (headers != null) {
            copiedHeaders.putAll(headers);
        }
        return HttpHeaders.readOnlyHttpHeaders(copiedHeaders);
    }

    /**
     * 复制映射并包装为只读对象。
     *
     * @param source 原始映射
     * @return 只读映射
     */
    private static Map<String, Object> copyMap(Map<String, ?> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> copiedMap = new LinkedHashMap<>(source);
        return Collections.unmodifiableMap(copiedMap);
    }

    /**
     * 响应提取器。
     *
     * @param <T> 提取结果类型
     */
    @FunctionalInterface
    private interface ResponseExtractor<T> {

        /**
         * 提取响应结果。
         *
         * @param responseSpec 响应规格
         * @return 提取结果
         */
        T extract(RestClient.ResponseSpec responseSpec);
    }

    /**
     * 不可变 HTTP 请求定义。
     *
     * @param method 请求方法
     * @param url 请求地址
     * @param queryParams 查询参数
     * @param uriVariables 路径变量
     * @param headers 请求头
     * @param body 请求体
     * @param contentType 请求内容类型
     * @param acceptTypes 接收内容类型
     */
    public record Request(
            HttpMethod method,
            String url,
            Map<String, Object> queryParams,
            Map<String, Object> uriVariables,
            HttpHeaders headers,
            Object body,
            MediaType contentType,
            List<MediaType> acceptTypes
    ) {

        /**
         * 构建不可变请求对象。
         */
        public Request {
            Objects.requireNonNull(method, "method must not be null");
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("url must not be blank");
            }
            queryParams = copyMap(queryParams);
            uriVariables = copyMap(uriVariables);
            headers = copyHeaders(headers);
            acceptTypes = acceptTypes == null || acceptTypes.isEmpty() ? List.of() : List.copyOf(acceptTypes);
        }

        /**
         * 创建通用请求对象。
         *
         * @param method 请求方法
         * @param url 请求地址
         * @return 请求对象
         */
        public static Request of(HttpMethod method, String url) {
            return new Request(method, url, null, null, null, null, null, null);
        }

        /**
         * 创建 GET 请求对象。
         *
         * @param url 请求地址
         * @return 请求对象
         */
        public static Request get(String url) {
            return of(HttpMethod.GET, url);
        }

        /**
         * 创建 POST 请求对象。
         *
         * @param url 请求地址
         * @param body 请求体
         * @return 请求对象
         */
        public static Request post(String url, Object body) {
            return of(HttpMethod.POST, url).withBody(body);
        }

        /**
         * 创建 PUT 请求对象。
         *
         * @param url 请求地址
         * @param body 请求体
         * @return 请求对象
         */
        public static Request put(String url, Object body) {
            return of(HttpMethod.PUT, url).withBody(body);
        }

        /**
         * 创建 PATCH 请求对象。
         *
         * @param url 请求地址
         * @param body 请求体
         * @return 请求对象
         */
        public static Request patch(String url, Object body) {
            return of(HttpMethod.PATCH, url).withBody(body);
        }

        /**
         * 创建 DELETE 请求对象。
         *
         * @param url 请求地址
         * @return 请求对象
         */
        public static Request delete(String url) {
            return of(HttpMethod.DELETE, url);
        }

        /**
         * 替换查询参数。
         *
         * @param queryParams 查询参数
         * @return 新请求对象
         */
        public Request withQueryParams(Map<String, ?> queryParams) {
            return new Request(method, url, copyMap(queryParams), uriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 增加单个查询参数。
         *
         * @param name 参数名
         * @param value 参数值
         * @return 新请求对象
         */
        public Request withQueryParam(String name, Object value) {
            Map<String, Object> updatedQueryParams = new LinkedHashMap<>(queryParams);
            updatedQueryParams.put(name, value);
            return new Request(method, url, updatedQueryParams, uriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 替换路径变量。
         *
         * @param uriVariables 路径变量
         * @return 新请求对象
         */
        public Request withUriVariables(Map<String, ?> uriVariables) {
            return new Request(method, url, queryParams, copyMap(uriVariables), headers, body, contentType, acceptTypes);
        }

        /**
         * 增加单个路径变量。
         *
         * @param name 变量名
         * @param value 变量值
         * @return 新请求对象
         */
        public Request withUriVariable(String name, Object value) {
            Map<String, Object> updatedUriVariables = new LinkedHashMap<>(uriVariables);
            updatedUriVariables.put(name, value);
            return new Request(method, url, queryParams, updatedUriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 替换请求头。
         *
         * @param headers 请求头
         * @return 新请求对象
         */
        public Request withHeaders(HttpHeaders headers) {
            return new Request(method, url, queryParams, uriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 增加单个请求头。
         *
         * @param name 请求头名称
         * @param values 请求头值
         * @return 新请求对象
         */
        public Request withHeader(String name, String... values) {
            HttpHeaders updatedHeaders = new HttpHeaders();
            updatedHeaders.putAll(headers);
            updatedHeaders.put(name, new ArrayList<>(List.of(values)));
            return new Request(method, url, queryParams, uriVariables, updatedHeaders, body, contentType, acceptTypes);
        }

        /**
         * 替换请求体。
         *
         * @param body 请求体
         * @return 新请求对象
         */
        public Request withBody(Object body) {
            return new Request(method, url, queryParams, uriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 指定请求内容类型。
         *
         * @param contentType 内容类型
         * @return 新请求对象
         */
        public Request withContentType(MediaType contentType) {
            return new Request(method, url, queryParams, uriVariables, headers, body, contentType, acceptTypes);
        }

        /**
         * 指定接收内容类型。
         *
         * @param acceptTypes 接收内容类型
         * @return 新请求对象
         */
        public Request withAccept(MediaType... acceptTypes) {
            return new Request(method, url, queryParams, uriVariables, headers, body, contentType, List.of(acceptTypes));
        }
    }
}
