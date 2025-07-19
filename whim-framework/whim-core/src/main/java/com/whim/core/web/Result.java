package com.whim.core.web;

import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * @date 2024/10/4 00:08
 * @description Result统一响应工具类
 */
@Data
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    private Result(T data) {
        this.data = data;
    }

    public Result<T> setResult(HttpStatus code, String message) {
        this.code = code.value();
        this.message = message;
        return this;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<T>(null).setResult(HttpStatus.OK, "success");
    }

    /**
     * 成功响应
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(String message) {
        return new Result<T>(null).setResult(HttpStatus.OK, message);
    }

    /**
     * 成功响应
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(data).setResult(HttpStatus.OK, message);
    }

    /**
     * 成功响应
     *
     * @param code    HttpStatus状态码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(HttpStatus code, String message, T data) {
        return new Result<>(data).setResult(code, message);
    }

    /**
     * 失败
     *
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error() {
        return new Result<T>(null).setResult(HttpStatus.INTERNAL_SERVER_ERROR, "error");
    }

    /**
     * 失败
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error(String message) {
        return new Result<T>(null).setResult(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 失败
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return Result<T>
     */

    public static <T> Result<T> error(String message, T data) {
        return new Result<>(data).setResult(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 失败
     *
     * @param code    HttpStatus 状态码
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error(HttpStatus code, String message) {
        return new Result<T>(null).setResult(code, message);
    }

    /**
     * 失败
     *
     * @param code    HttpStatus 状态码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> error(HttpStatus code, String message, T data) {
        return new Result<>(data).setResult(code, message);
    }

    /**
     * 参数异常
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> validationError(String message) {
        return new Result<T>(null).setResult(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 参数错误
     * key为参数名称，value为错误提示
     *
     * @param <T> 数据类型
     * @return Result<LinkedHashMap < String, String>>
     */
    public static <T> Result<List<Map<String, String>>> validationError(List<Map<String, String>> errorInfo) {
        return new Result<>(errorInfo).setResult(HttpStatus.BAD_REQUEST, "参数错误");
    }

    /**
     * 参数错误
     * key为参数名称，value为错误提示
     *
     * @param message   消息
     * @param errorInfo 错误信息
     * @param <T>       数据类型
     * @return Result<LinkedHashMap < String, String>>
     */
    public static <T> Result<List<Map<String, String>>> validationError(String message, List<Map<String, String>> errorInfo) {
        return new Result<>(errorInfo).setResult(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 获取文件
     *
     * @param resource 资源
     * @return ResponseEntity<Resource>
     * @throws IOException 异常
     */
    public static ResponseEntity<Resource> file(Resource resource) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            Path filePath = Path.of(resource.getURI());
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
            }
            // 为什么会注释CONTENT_DISPOSITION 允许浏览器根据文件类型自行决定如何处理文件是查看还是下载，否则不管是图片还是文件都默认是下载
            // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UriUtils.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8) + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, mimeType);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new IOException("返回文件时发生错误");
        }
    }

    /**
     * 未认证
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<T>(null).setResult(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 没有权限
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result<T>
     */
    public static <T> Result<T> permissionDenied(String message) {
        return new Result<T>(null).setResult(HttpStatus.FORBIDDEN, message);
    }


}
