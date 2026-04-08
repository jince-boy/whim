package com.whim.web.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/04/08
 * @description 对请求参数和 JSON 请求体执行 XSS 清洗的请求包装器。
 */
@Slf4j
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private static final PolicyFactory PLAIN_TEXT_POLICY = new HtmlPolicyBuilder().toFactory();
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private byte[] sanitizedBody;

    /**
     * 创建 XSS 请求包装器。
     *
     * @param request 原始请求
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取清洗后的单个请求参数。
     *
     * @param name 参数名
     * @return 清洗后的参数值
     */
    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    /**
     * 获取清洗后的请求参数数组。
     *
     * @param name 参数名
     * @return 清洗后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        return cleanValues(super.getParameterValues(name));
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
            cleanedValues[index] = clean(values[index]);
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
        if (sanitizedBody != null) {
            return sanitizedBody;
        }
        byte[] originalBody = StreamUtils.copyToByteArray(super.getInputStream());
        sanitizedBody = isJsonRequest() ? cleanJsonBody(originalBody) : originalBody;
        return sanitizedBody;
    }

    /**
     * 清洗普通字符串中的 XSS 内容。
     *
     * @param value 原始字符串
     * @return 清洗后的字符串
     */
    private String clean(String value) {
        if (value == null) {
            return null;
        }
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return PLAIN_TEXT_POLICY.sanitize(value);
    }

    /**
     * 清洗 JSON 请求体中的所有文本节点。
     *
     * @param requestBody 原始请求体字节数组
     * @return 清洗后的请求体字节数组
     */
    private byte[] cleanJsonBody(byte[] requestBody) {
        if (requestBody == null || requestBody.length == 0) {
            return requestBody;
        }
        try {
            var rootNode = JSON_MAPPER.readTree(requestBody);
            if (rootNode == null) {
                return requestBody;
            }
            return JSON_MAPPER.writeValueAsBytes(cleanJsonNode(rootNode));
        } catch (Exception exception) {
            log.warn("XSS JSON 请求体清洗失败，已回退为原始请求体，uri={}", super.getRequestURI(), exception);
            return requestBody;
        }
    }

    /**
     * 递归清洗 JSON 树中的文本节点。
     *
     * @param jsonNode 当前 JSON 节点
     * @return 清洗后的 JSON 节点
     */
    private JsonNode cleanJsonNode(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isString()) {
            return JsonNodeFactory.instance.stringNode(clean(jsonNode.stringValue()));
        }
        if (jsonNode.isObject()) {
            return cleanObjectNode((ObjectNode) jsonNode);
        }
        if (jsonNode.isArray()) {
            return cleanArrayNode((ArrayNode) jsonNode);
        }
        return jsonNode;
    }

    /**
     * 清洗 JSON 对象节点中的所有字段。
     *
     * @param objectNode JSON 对象节点
     * @return 清洗后的对象节点
     */
    private ObjectNode cleanObjectNode(ObjectNode objectNode) {
        for (Map.Entry<String, JsonNode> property : objectNode.properties()) {
            objectNode.set(property.getKey(), cleanJsonNode(property.getValue()));
        }
        return objectNode;
    }

    /**
     * 清洗 JSON 数组节点中的所有元素。
     *
     * @param arrayNode JSON 数组节点
     * @return 清洗后的数组节点
     */
    private ArrayNode cleanArrayNode(ArrayNode arrayNode) {
        for (int index = 0; index < arrayNode.size(); index++) {
            arrayNode.set(index, cleanJsonNode(arrayNode.get(index)));
        }
        return arrayNode;
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
        try {
            return Charset.forName(characterEncoding);
        } catch (Exception exception) {
            log.warn("解析请求字符集失败，已使用 UTF-8，encoding={}, uri={}", characterEncoding, super.getRequestURI(), exception);
            return StandardCharsets.UTF_8;
        }
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
