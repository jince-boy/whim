package com.whim.web.wrapper;

import com.whim.web.xss.XssCleaner;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/08
 * @description 对请求参数和 JSON 请求体执行 XSS 清洗的请求包装器。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final XssCleaner xssCleaner;
    private byte[] cachedBody;
    private byte[] resolvedBody;

    /**
     * 创建 XSS 请求包装器。
     *
     * @param request 原始请求
     * @param xssCleaner XSS 清洗器
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, XssCleaner xssCleaner) {
        super(request);
        this.xssCleaner = Objects.requireNonNull(xssCleaner, "参数[xssCleaner]不能为空");
    }

    /**
     * 获取清洗后的单个请求参数。
     *
     * @param name 参数名
     * @return 清洗后的参数值
     */
    @Override
    public String getParameter(String name) {
        return xssCleaner.clean(super.getParameter(name));
    }

    /**
     * 获取清洗后的请求参数数组。
     *
     * @param name 参数名
     * @return 清洗后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null || values.length == 0) {
            return values;
        }
        String[] cleanedValues = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            cleanedValues[index] = xssCleaner.clean(values[index]);
        }
        return cleanedValues;
    }

    /**
     * 获取清洗后的请求参数映射。
     *
     * @return 清洗后的参数映射
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> sourceMap = super.getParameterMap();
        Map<String, String[]> cleanedMap = new LinkedHashMap<>(sourceMap.size());
        sourceMap.forEach((key, values) -> cleanedMap.put(key, cleanValues(values)));
        return cleanedMap;
    }

    /**
     * 获取清洗后的请求输入流。
     *
     * @return 清洗后的输入流
     * @throws IOException I/O 异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(resolveRequestBody());
    }

    /**
     * 获取清洗后的请求字符流。
     *
     * @return 清洗后的字符流
     * @throws IOException I/O 异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(resolveRequestBody()), resolveCharset()));
    }

    /**
     * 获取清洗后请求体的长度。
     *
     * @return 请求体长度
     */
    @Override
    public int getContentLength() {
        try {
            return resolveRequestBody().length;
        } catch (IOException exception) {
            return -1;
        }
    }

    /**
     * 获取清洗后请求体的长度。
     *
     * @return 请求体长度
     */
    @Override
    public long getContentLengthLong() {
        try {
            return resolveRequestBody().length;
        } catch (IOException exception) {
            return -1;
        }
    }

    /**
     * 清洗参数值数组。
     *
     * @param values 原始参数值数组
     * @return 清洗后的参数值数组
     */
    private String[] cleanValues(String[] values) {
        if (values == null || values.length == 0) {
            return values;
        }
        String[] cleanedValues = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            cleanedValues[index] = xssCleaner.clean(values[index]);
        }
        return cleanedValues;
    }

    /**
     * 解析请求体内容并在需要时执行 JSON 清洗。
     *
     * @return 处理后的请求体字节数组
     * @throws IOException I/O 异常
     */
    private byte[] resolveRequestBody() throws IOException {
        if (resolvedBody != null) {
            return resolvedBody;
        }
        byte[] originalBody = readCachedBody();
        resolvedBody = isJsonRequest() ? xssCleaner.cleanJsonBody(originalBody) : originalBody;
        return resolvedBody;
    }

    /**
     * 缓存原始请求体字节数组。
     *
     * @return 原始请求体字节数组
     * @throws IOException I/O 异常
     */
    private byte[] readCachedBody() throws IOException {
        if (cachedBody == null) {
            cachedBody = StreamUtils.copyToByteArray(super.getInputStream());
        }
        return cachedBody;
    }

    /**
     * 判断当前请求是否为 JSON 请求。
     *
     * @return true 表示 JSON 请求
     */
    private boolean isJsonRequest() {
        String contentType = super.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        return normalizedContentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
                || normalizedContentType.contains("+json");
    }

    /**
     * 解析请求字符集。
     *
     * @return 请求字符集
     */
    private Charset resolveCharset() {
        String characterEncoding = super.getCharacterEncoding();
        if (!StringUtils.hasText(characterEncoding)) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(characterEncoding);
    }

    /**
     * 基于缓存请求体实现 ServletInputStream。
     */
    private static final class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;
        private ReadListener readListener;
        private boolean allDataReadNotified;

        /**
         * 创建缓存请求体输入流。
         *
         * @param cachedBody 缓存请求体
         */
        private CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        /**
         * 读取单个字节。
         *
         * @return 读取到的字节
         * @throws IOException I/O 异常
         */
        @Override
        public int read() throws IOException {
            int read = this.inputStream.read();
            notifyIfAllDataRead();
            return read;
        }

        /**
         * 读取字节数组。
         *
         * @param buffer 目标缓冲区
         * @param offset 起始偏移量
         * @param length 读取长度
         * @return 实际读取长度
         * @throws IOException I/O 异常
         */
        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = this.inputStream.read(buffer, offset, length);
            notifyIfAllDataRead();
            return read;
        }

        /**
         * 判断数据是否已经读取完成。
         *
         * @return true 表示读取完成
         */
        @Override
        public boolean isFinished() {
            return this.inputStream.available() == 0;
        }

        /**
         * 判断当前输入流是否可读。
         *
         * @return true 表示可读
         */
        @Override
        public boolean isReady() {
            return true;
        }

        /**
         * 设置异步读取监听器。
         *
         * @param readListener 读取监听器
         */
        @Override
        public void setReadListener(ReadListener readListener) {
            if (readListener == null) {
                throw new IllegalArgumentException("参数[readListener]不能为空");
            }
            this.readListener = readListener;
            try {
                if (!isFinished()) {
                    readListener.onDataAvailable();
                }
                notifyIfAllDataRead();
            } catch (IOException exception) {
                readListener.onError(exception);
            }
        }

        /**
         * 在数据读取完毕后通知监听器。
         */
        private void notifyIfAllDataRead() {
            if (this.readListener == null || this.allDataReadNotified || !isFinished()) {
                return;
            }
            this.allDataReadNotified = true;
            try {
                this.readListener.onAllDataRead();
            } catch (IOException exception) {
                this.readListener.onError(exception);
            }
        }
    }
}
