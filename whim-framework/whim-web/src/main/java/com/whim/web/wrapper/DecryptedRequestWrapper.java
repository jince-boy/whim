package com.whim.web.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 为后续过滤器和 MVC 提供解密后的 UTF-8 JSON 请求体及一致的长度信息。
 */
public class DecryptedRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    /**
     * 创建解密后的请求包装器。
     *
     * @param request 原始请求
     * @param body 解密后的业务 JSON
     */
    public DecryptedRequestWrapper(HttpServletRequest request, byte[] body) {
        super(request);
        this.body = body;
    }

    /**
     * 获取业务 JSON 的同步 Servlet 输入流。
     *
     * @return 解密后的输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        return new BodyInputStream(body);
    }

    /**
     * 获取 UTF-8 请求字符流。
     *
     * @return 请求字符流
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * 获取解密后的请求体长度。
     *
     * @return 请求体字节数
     */
    @Override
    public int getContentLength() {
        return body.length;
    }

    /**
     * 获取解密后的请求体长度。
     *
     * @return 请求体字节数
     */
    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    /**
     * 获取解密后的媒体类型。
     *
     * @return JSON 媒体类型
     */
    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    /**
     * 获取解密后的字符集。
     *
     * @return UTF-8 字符集名称
     */
    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    /**
     * 同步请求头中的业务 JSON 类型与长度。
     *
     * @param name 请求头名称
     * @return 请求头值
     */
    @Override
    public String getHeader(String name) {
        if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
            return Integer.toString(body.length);
        }
        if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
            return getContentType();
        }
        return super.getHeader(name);
    }

    /**
     * 同步请求头集合中的类型与长度。
     *
     * @param name 请求头名称
     * @return 请求头值集合
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name) || HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
            return Collections.enumeration(Collections.singletonList(getHeader(name)));
        }
        return super.getHeaders(name);
    }

    /**
     * 同步整数形式的内容长度。
     *
     * @param name 请求头名称
     * @return 整数请求头值
     */
    @Override
    public int getIntHeader(String name) {
        return HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name) ? body.length : super.getIntHeader(name);
    }

    /**
     * @author Jince
     * @date 2026/10/25
     * @description 供同步 MVC JSON 反序列化使用的内存输入流。
     */
    private static class BodyInputStream extends ServletInputStream {
        private final ByteArrayInputStream input;

        /**
         * 创建内存输入流。
         *
         * @param body 请求体字节
         */
        private BodyInputStream(byte[] body) {
            input = new ByteArrayInputStream(body);
        }

        /**
         * 读取一个字节。
         *
         * @return 字节或结束标记
         */
        @Override
        public int read() {
            return input.read();
        }

        /**
         * 批量读取字节。
         *
         * @param buffer 目标缓冲区
         * @param offset 写入偏移量
         * @param length 最大读取长度
         * @return 实际读取长度
         */
        @Override
        public int read(byte[] buffer, int offset, int length) {
            return input.read(buffer, offset, length);
        }

        /**
         * 判断输入流是否读取完毕。
         *
         * @return 是否读取完毕
         */
        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        /**
         * 判断输入流是否可以读取。
         *
         * @return 始终可读取
         */
        @Override
        public boolean isReady() {
            return true;
        }

        /**
         * 拒绝非同步读取，当前加密接口仅支持同步 JSON 请求体。
         *
         * @param listener 异步读取监听器
         */
        @Override
        public void setReadListener(ReadListener listener) {
            throw new IllegalStateException("加密 JSON 请求体不支持非阻塞读取");
        }
    }
}
