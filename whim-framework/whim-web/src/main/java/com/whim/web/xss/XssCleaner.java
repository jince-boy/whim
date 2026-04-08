package com.whim.web.xss;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.json.JsonMapper;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/04/08
 * @description 提供统一的 XSS 清洗能力。
 */
public final class XssCleaner {
    private static final PolicyFactory PLAIN_TEXT_POLICY = new HtmlPolicyBuilder().toFactory();
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    /**
     * 清洗普通字符串中的 XSS 内容。
     *
     * @param value 原始字符串
     * @return 清洗后的字符串
     */
    public String clean(String value) {
        if (value == null) {
            return null;
        }
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return PLAIN_TEXT_POLICY.sanitize(value).trim();
    }

    /**
     * 清洗 JSON 请求体中的所有文本节点。
     *
     * @param requestBody 原始请求体字节数组
     * @return 清洗后的请求体字节数组
     */
    public byte[] cleanJsonBody(byte[] requestBody) {
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
}
