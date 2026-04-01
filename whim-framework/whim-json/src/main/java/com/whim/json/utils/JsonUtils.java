package com.whim.json.utils;

import lombok.AccessLevel;
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
    private static volatile JsonMapper jsonMapper;

    /**
     * 获取当前使用的 JsonMapper 实例。
     *
     * @return JsonMapper 实例
     */
    public static JsonMapper getJsonMapper() {
        return requireJsonMapper();
    }

    /**
     * 替换当前 JsonMapper 实例。
     *
     * @param jsonMapper JsonMapper 实例
     */
    public static void setJsonMapper(JsonMapper jsonMapper) {
        JsonUtils.jsonMapper = Objects.requireNonNull(jsonMapper, "参数[jsonMapper]不能为空");
    }

    /**
     * 重置当前 JsonMapper 实例。
     */
    public static void resetJsonMapper() {
        jsonMapper = null;
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public static String toJson(Object value) {
        try {
            return requireJsonMapper().writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将对象序列化为 JSON 失败。", exception);
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
            return requireJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将对象序列化为格式化 JSON 失败。", exception);
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
            return requireJsonMapper().writeValueAsBytes(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将对象序列化为 JSON 字节数组失败。", exception);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().readValue(requireHasText(json, "json"), targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将 JSON 反序列化为 %s 失败。".formatted(targetType.getName()), exception);
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
        Objects.requireNonNull(jsonBytes, "参数[jsonBytes]不能为空");
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().readValue(jsonBytes, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将 JSON 字节数组反序列化为 %s 失败。".formatted(targetType.getName()), exception);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().readValue(requireHasText(json, "json"), targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将 JSON 反序列化为泛型目标类型失败。", exception);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        return fromJson(requireHasText(json, "json"), requireJsonMapper().getTypeFactory().constructType(targetType.getType()));
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
        Objects.requireNonNull(elementType, "参数[elementType]不能为空");
        JavaType listType = requireJsonMapper().getTypeFactory().constructCollectionType(List.class, elementType);
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
        Objects.requireNonNull(keyType, "参数[keyType]不能为空");
        Objects.requireNonNull(valueType, "参数[valueType]不能为空");
        JavaType mapType = requireJsonMapper().getTypeFactory().constructMapType(Map.class, keyType, valueType);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().convertValue(source, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将值转换为 %s 失败。".formatted(targetType.getName()), exception);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().convertValue(source, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将值转换为泛型目标类型失败。", exception);
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
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        return convert(source, requireJsonMapper().getTypeFactory().constructType(targetType.getType()));
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
            return requireJsonMapper().valueToTree(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将值转换为 JSON 树失败。", exception);
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
            return requireJsonMapper().readTree(requireHasText(json, "json"));
        } catch (Exception exception) {
            throw new IllegalArgumentException("读取 JSON 树失败。", exception);
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
        Objects.requireNonNull(jsonNode, "参数[jsonNode]不能为空");
        Objects.requireNonNull(targetType, "参数[targetType]不能为空");
        try {
            return requireJsonMapper().treeToValue(jsonNode, targetType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将 JSON 树转换为 %s 失败。".formatted(targetType.getName()), exception);
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
        Objects.requireNonNull(javaType, "参数[javaType]不能为空");
        try {
            return requireJsonMapper().readValue(requireHasText(json, "json"), javaType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将 JSON 反序列化为 JavaType 失败。", exception);
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
        Objects.requireNonNull(javaType, "参数[javaType]不能为空");
        try {
            return requireJsonMapper().convertValue(source, javaType);
        } catch (Exception exception) {
            throw new IllegalArgumentException("将值转换为 JavaType 失败。", exception);
        }
    }

    private static JsonMapper requireJsonMapper() {
        if (jsonMapper == null) {
            throw new IllegalStateException("JsonUtils 尚未初始化 JsonMapper");
        }
        return jsonMapper;
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
            throw new IllegalArgumentException("参数[%s]不能为空白".formatted(fieldName));
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
