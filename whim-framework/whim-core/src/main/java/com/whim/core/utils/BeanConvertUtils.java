package com.whim.core.utils;

import io.github.linpeilie.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/3/24
 * @description Bean 转换工具类，统一封装 MapStruct Plus 的对象映射能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanConvertUtils {

    /**
     * 获取转换器实例。
     *
     * @return 转换器实例
     */
    private static Converter getConverter() {
        return SpringUtils.getBean(Converter.class);
    }

    /**
     * 将源对象转换为目标类型。
     *
     * @param source 源对象
     * @param targetType 目标类型
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (source == null) {
            return null;
        }
        return getConverter().convert(source, targetType);
    }

    /**
     * 将源对象转换到目标对象。
     *
     * @param source 源对象
     * @param target 目标对象
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, T target) {
        Objects.requireNonNull(target, "target must not be null");
        if (source == null) {
            return null;
        }
        return getConverter().convert(source, target);
    }

    /**
     * 将源列表转换为目标类型列表。
     *
     * @param sourceList 源列表
     * @param targetType 目标类型
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return getConverter().convert(sourceList, targetType);
    }

    /**
     * 将参数映射转换为目标对象。
     *
     * @param sourceMap 源参数映射
     * @param targetType 目标类型
     * @param <T> 目标类型
     * @return 转换后的对象
     */
    public static <T> T convertMap(Map<String, ?> sourceMap, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (sourceMap == null || sourceMap.isEmpty()) {
            return null;
        }
        return getConverter().convert(sourceMap, targetType);
    }
}
