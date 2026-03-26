package com.whim.core.web;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/26
 * @description 统一接口响应模型
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    @NonNull
    private final String message;
    private final T data;

    /**
     * 判断当前响应是否成功
     *
     * @return 是否为 2xx 状态
     */
    public boolean isSuccess() {
        return HttpStatusCode.valueOf(code).is2xxSuccessful();
    }

    /**
     * 将当前响应转换为 ResponseEntity
     *
     * @return HTTP 响应实体
     */
    public ResponseEntity<Result<T>> toResponseEntity() {
        return ResponseEntity.status(HttpStatusCode.valueOf(code)).body(this);
    }

    /**
     * 构建不带数据的成功响应
     *
     * @return 成功响应
     */
    public static Result<Void> success() {
        return of(HttpStatus.OK, "success", null);
    }

    /**
     * 构建不带数据的成功响应
     *
     * @param message 响应消息
     * @return 成功响应
     */
    public static Result<Void> success(String message) {
        return of(HttpStatus.OK, message, null);
    }

    /**
     * 构建带数据的成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return of(HttpStatus.OK, "success", data);
    }

    /**
     * 构建带数据的成功响应
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return of(HttpStatus.OK, message, data);
    }

    /**
     * 构建指定状态码的成功响应
     *
     * @param status  HTTP 状态码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(HttpStatusCode status, String message, T data) {
        validateSuccessStatus(status);
        return of(status, message, data);
    }

    /**
     * 构建不带数据的失败响应
     *
     * @return 失败响应
     */
    public static Result<Void> error() {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, "error", null);
    }

    /**
     * 构建不带数据的失败响应
     *
     * @param message 响应消息
     * @return 失败响应
     */
    public static Result<Void> error(String message) {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    /**
     * 构建带数据的失败响应
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(String message, T data) {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, message, data);
    }

    /**
     * 构建不带数据的失败响应
     *
     * @param status  HTTP 状态码
     * @param message 响应消息
     * @return 失败响应
     */
    public static Result<Void> error(HttpStatusCode status, String message) {
        validateErrorStatus(status);
        return of(status, message, null);
    }

    /**
     * 构建带数据的失败响应
     *
     * @param status  HTTP 状态码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(HttpStatusCode status, String message, T data) {
        validateErrorStatus(status);
        return of(status, message, data);
    }

    /**
     * 构建不带明细的参数校验失败响应
     *
     * @param message 校验消息
     * @return 参数校验失败响应
     */
    public static Result<Void> validationError(String message) {
        return of(HttpStatus.BAD_REQUEST, message, null);
    }

    /**
     * 构建带字段明细的参数校验失败响应
     *
     * @param errorInfo 字段校验明细
     * @return 参数校验失败响应
     */
    public static Result<List<ValidationError>> validationError(List<ValidationError> errorInfo) {
        return of(HttpStatus.BAD_REQUEST, "参数错误", List.copyOf(errorInfo));
    }

    /**
     * 构建带字段明细的参数校验失败响应
     *
     * @param message   校验消息
     * @param errorInfo 字段校验明细
     * @return 参数校验失败响应
     */
    public static Result<List<ValidationError>> validationError(String message, List<ValidationError> errorInfo) {
        return of(HttpStatus.BAD_REQUEST, message, List.copyOf(errorInfo));
    }

    /**
     * 创建字段校验错误明细
     *
     * @param field   字段名
     * @param message 校验消息
     * @return 校验错误明细
     */
    public static ValidationError fieldError(String field, String message) {
        return ValidationError.of(field, message);
    }

    /**
     * 构建文件响应
     *
     * @param resource 文件资源
     * @return 文件响应实体
     */
    public static ResponseEntity<Resource> file(Resource resource) {
        Objects.requireNonNull(resource, "resource must not be null");

        var headers = new HttpHeaders();
        var mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentType(mediaType);

        try {
            headers.setContentLength(resource.contentLength());
        } catch (IOException ignored) {
            // 流式资源可能无法提前获取内容长度。
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    /**
     * 构建未认证响应
     *
     * @param message 响应消息
     * @return 未认证响应
     */
    public static Result<Void> unauthorized(String message) {
        return of(HttpStatus.UNAUTHORIZED, message, null);
    }

    /**
     * 构建无权限响应
     *
     * @param message 响应消息
     * @return 无权限响应
     */
    public static Result<Void> permissionDenied(String message) {
        return of(HttpStatus.FORBIDDEN, message, null);
    }

    /**
     * 根据 HTTP 状态码创建响应对象
     *
     * @param status  HTTP 状态码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 响应对象
     */
    private static <T> Result<T> of(HttpStatusCode status, String message, T data) {
        Objects.requireNonNull(status, "status must not be null");
        return new Result<>(status.value(), message, data);
    }

    /**
     * 校验成功响应状态码是否合法
     *
     * @param status HTTP 状态码
     */
    private static void validateSuccessStatus(HttpStatusCode status) {
        if (!status.is2xxSuccessful()) {
            throw new IllegalArgumentException("success status must be a 2xx status");
        }
    }

    /**
     * 校验失败响应状态码是否合法
     *
     * @param status HTTP 状态码
     */
    private static void validateErrorStatus(HttpStatusCode status) {
        if (status.is2xxSuccessful()) {
            throw new IllegalArgumentException("error status must not be a 2xx status");
        }
    }

    /**
     * @author Jince
     * @date 2026/03/26
     * @description 字段校验错误明细
     */
    @Getter
    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ValidationError implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NonNull
        private final String field;
        @NonNull
        private final String message;

        /**
         * 创建字段校验错误明细
         *
         * @param field   字段名
         * @param message 校验消息
         * @return 校验错误明细
         */
        private static ValidationError of(String field, String message) {
            return new ValidationError(field, message);
        }
    }
}
