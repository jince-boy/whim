package com.whim.json.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whim.core.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jince
 * date: 2025/6/17 21:56
 * description: JSON工具类
 */
public class JsonUtils {

    private static volatile ObjectMapper objectMapper;
    private static final Map<Class<?>, JavaType> CACHED_TYPES = new ConcurrentHashMap<>();

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            synchronized (JsonUtils.class) {
                if (objectMapper == null) {
                    objectMapper = SpringUtils.getBean(ObjectMapper.class);
                }
            }
        }
        return objectMapper;
    }

    // 通用读取方法
    private static <T> T readValue(String text, JavaType type) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return getObjectMapper().readValue(text, type);
        } catch (IOException e) {
            throw new RuntimeException("JSON解析失败: " + text, e);
        }
    }

    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return readValue(text, getObjectMapper().getTypeFactory().constructType(clazz));
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return getObjectMapper().readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("字节数组解析失败", e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        return readValue(text, getObjectMapper().getTypeFactory().constructType(typeReference));
    }

    /**
     * 解析JSON字符串为Map<String, Object>
     * @param text JSON字符串
     * @return Map对象，解析失败返回null
     */
    public static Map<String, Object> parseMap(String text) {
        try {
            return parseObject(text, new TypeReference<Map<String, Object>>() {});
        } catch (RuntimeException e) {
            return null; // 保持原逻辑兼容性
        }
    }

    /**
     * 解析JSON字符串为List<Map<String, Object>>
     * @param text JSON字符串
     * @return List of Maps，解析失败返回null
     */
    public static List<Map<String, Object>> parseArrayMap(String text) {
        JavaType type = CACHED_TYPES.computeIfAbsent(Map.class,
                k -> getObjectMapper().getTypeFactory().constructParametricType(List.class,
                        getObjectMapper().getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
        return readValue(text, type);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        JavaType type = CACHED_TYPES.computeIfAbsent(clazz,
                k -> getObjectMapper().getTypeFactory().constructCollectionType(List.class, clazz));
        return readValue(text, type);
    }
}
