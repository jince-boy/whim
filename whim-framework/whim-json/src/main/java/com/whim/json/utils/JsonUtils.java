package com.whim.json.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Jince
 * @date 2026/03/27
 * @description JSON 工具类，封装 Spring Boot 4 下 Jackson 3 的常用序列化、反序列化、树模型与泛型转换能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    /**
     * 当前使用的 JsonMapper 实例。
     */
    @Getter
    private static volatile JsonMapper jsonMapper = createDefaultJsonMapper();

    /**
     * 替换当前 JsonMapper 实例。
     *
     * @param jsonMapper JsonMapper 实例
     */
    public static void setJsonMapper(JsonMapper jsonMapper) {
        JsonUtils.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper must not be null");
    }

    /**
     * 重置为默认 JsonMapper 实例。
     */
    public static void resetJsonMapper() {
        jsonMapper = createDefaultJsonMapper();
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public static String toJson(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize object to JSON.", exception);
        }
    }

    /**
     * 将对象序列化为格式化后的 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return 格式化后的 JSON 字符串
     */
    public static String toPrettyJson(Object value) {
        try {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize object to pretty JSON.", exception);
        }
    }

    /**
     * 将对象序列化为 JSON 字节数组。
     *
     * @param value 待序列化对象
     * @return JSON 字节数组
     */
    public static byte[] toBytes(Object value) {
        try {
            return jsonMapper.writeValueAsBytes(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize object to JSON bytes.", exception);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象。
     *
     * @param json JSON 字符串
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.readValue(requireHasText(json, "json"), targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize JSON to %s.".formatted(targetType.getName()), exception);
        }
    }

    /**
     * 将 JSON 字节数组反序列化为指定类型对象。
     *
     * @param jsonBytes JSON 字节数组
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public static <T> T fromJson(byte[] jsonBytes, Class<T> targetType) {
        Objects.requireNonNull(jsonBytes, "jsonBytes must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.readValue(jsonBytes, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize JSON bytes to %s.".formatted(targetType.getName()), exception);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定泛型对象。
     *
     * @param json JSON 字符串
     * @param targetType 目标泛型类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, TypeReference<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.readValue(requireHasText(json, "json"), targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize JSON to generic target type.", exception);
        }
    }

    /**
     * 将 JSON 字符串反序列化为 Spring 泛型类型对象。
     *
     * @param json JSON 字符串
     * @param targetType Spring 泛型类型
     * @param <T> 目标泛型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, ParameterizedTypeReference<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        return fromJson(requireHasText(json, "json"), jsonMapper.getTypeFactory().constructType(targetType.getType()));
    }

    /**
     * 将 JSON 字符串反序列化为列表。
     *
     * @param json JSON 字符串
     * @param elementType 元素类型
     * @param <T> 元素泛型
     * @return 列表对象
     */
    public static <T> List<T> toList(String json, Class<T> elementType) {
        Objects.requireNonNull(elementType, "elementType must not be null");
        JavaType listType = jsonMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return fromJson(json, listType);
    }

    /**
     * 将 JSON 字符串反序列化为映射。
     *
     * @param json JSON 字符串
     * @param keyType 键类型
     * @param valueType 值类型
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 映射对象
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyType, Class<V> valueType) {
        Objects.requireNonNull(keyType, "keyType must not be null");
        Objects.requireNonNull(valueType, "valueType must not be null");
        JavaType mapType = jsonMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        return fromJson(json, mapType);
    }

    /**
     * 尝试将 JSON 字符串反序列化为指定类型对象。
     *
     * @param json JSON 字符串
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 解析结果
     */
    public static <T> Optional<T> tryFromJson(String json, Class<T> targetType) {
        if (isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(fromJson(json, targetType));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 尝试将 JSON 字符串反序列化为 Spring 泛型类型对象。
     *
     * @param json JSON 字符串
     * @param targetType Spring 泛型类型
     * @param <T> 目标泛型
     * @return 解析结果
     */
    public static <T> Optional<T> tryFromJson(String json, ParameterizedTypeReference<T> targetType) {
        if (isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(fromJson(json, targetType));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 将对象转换为指定类型。
     *
     * @param source 原始对象
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.convertValue(source, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to convert value to %s.".formatted(targetType.getName()), exception);
        }
    }

    /**
     * 将对象转换为指定泛型类型。
     *
     * @param source 原始对象
     * @param targetType 目标泛型类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    public static <T> T convert(Object source, TypeReference<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.convertValue(source, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to convert value to generic target type.", exception);
        }
    }

    /**
     * 将对象转换为 Spring 泛型类型。
     *
     * @param source 原始对象
     * @param targetType Spring 泛型类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    public static <T> T convert(Object source, ParameterizedTypeReference<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        return convert(source, jsonMapper.getTypeFactory().constructType(targetType.getType()));
    }

    /**
     * 尝试将对象转换为指定类型。
     *
     * @param source 原始对象
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    public static <T> Optional<T> tryConvert(Object source, Class<T> targetType) {
        try {
            return Optional.ofNullable(convert(source, targetType));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 将对象转换为树模型。
     *
     * @param value 原始对象
     * @return 树模型节点
     */
    public static JsonNode toTree(Object value) {
        try {
            return jsonMapper.valueToTree(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to convert value to JSON tree.", exception);
        }
    }

    /**
     * 将 JSON 字符串读取为树模型。
     *
     * @param json JSON 字符串
     * @return 树模型节点
     */
    public static JsonNode readTree(String json) {
        try {
            return jsonMapper.readTree(requireHasText(json, "json"));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to read JSON tree.", exception);
        }
    }

    /**
     * 尝试将 JSON 字符串读取为树模型。
     *
     * @param json JSON 字符串
     * @return 树模型节点
     */
    public static Optional<JsonNode> tryReadTree(String json) {
        if (isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(readTree(json));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    /**
     * 将树模型转换为指定类型对象。
     *
     * @param jsonNode 树模型节点
     * @param targetType 目标类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    public static <T> T treeToValue(JsonNode jsonNode, Class<T> targetType) {
        Objects.requireNonNull(jsonNode, "jsonNode must not be null");
        Objects.requireNonNull(targetType, "targetType must not be null");
        try {
            return jsonMapper.treeToValue(jsonNode, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to convert JSON tree to %s.".formatted(targetType.getName()), exception);
        }
    }

    /**
     * 判断文本是否为合法 JSON。
     *
     * @param json JSON 字符串
     * @return 是否合法
     */
    public static boolean isValidJson(String json) {
        return tryReadTree(json).isPresent();
    }

    /**
     * 判断文本是否为 JSON 对象。
     *
     * @param json JSON 字符串
     * @return 是否为对象
     */
    public static boolean isJsonObject(String json) {
        return tryReadTree(json).map(JsonNode::isObject).orElse(false);
    }

    /**
     * 判断文本是否为 JSON 数组。
     *
     * @param json JSON 字符串
     * @return 是否为数组
     */
    public static boolean isJsonArray(String json) {
        return tryReadTree(json).map(JsonNode::isArray).orElse(false);
    }

    /**
     * 使用 JavaType 反序列化 JSON。
     *
     * @param json JSON 字符串
     * @param javaType Jackson 类型
     * @param <T> 目标泛型
     * @return 反序列化结果
     */
    private static <T> T fromJson(String json, JavaType javaType) {
        Objects.requireNonNull(javaType, "javaType must not be null");
        try {
            return jsonMapper.readValue(requireHasText(json, "json"), javaType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize JSON to JavaType.", exception);
        }
    }

    /**
     * 使用 JavaType 转换对象。
     *
     * @param source 原始对象
     * @param javaType Jackson 类型
     * @param <T> 目标泛型
     * @return 转换结果
     */
    private static <T> T convert(Object source, JavaType javaType) {
        Objects.requireNonNull(javaType, "javaType must not be null");
        try {
            return jsonMapper.convertValue(source, javaType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to convert value to JavaType.", exception);
        }
    }

    /**
     * 创建默认 JsonMapper。
     *
     * @return 默认 JsonMapper
     */
    private static JsonMapper createDefaultJsonMapper() {
        return JsonMapper.builder().build();
    }

    /**
     * 校验文本必须非空白。
     *
     * @param text 文本内容
     * @param fieldName 字段名
     * @return 原始文本
     */
    private static String requireHasText(String text, String fieldName) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    /**
     * 判断文本是否为空白。
     *
     * @param text 文本内容
     * @return 是否为空白
     */
    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /**
     * @author Jince
     * @date 2026/03/27
     * @description JsonUtils 初始化器，用于接入 Spring Boot 自动配置好的 JsonMapper。
     */
    public static final class Initializer {

        /**
         * 初始化 JsonUtils 使用的 JsonMapper。
         *
         * @param jsonMapper Spring Boot 管理的 JsonMapper
         */
        public Initializer(JsonMapper jsonMapper) {
            JsonUtils.setJsonMapper(jsonMapper);
        }
    }
}
