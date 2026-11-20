package com.whim.log.aspect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.whim.json.utils.JsonUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 采集控制器绑定的业务参数，在各层应用当前操作声明的字段排除规则。
 */
final class OperationLogRequestSnapshot {
    private static final int MAX_ITEMS = 50;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_STRING_LENGTH = 2048;
    private static final Object OMITTED = new Object();

    private final Set<String> excludedNames;
    // request_param 使用 MySQL TEXT，给最外层结构和截断标记预留空间。
    private int remainingBytes = 60 * 1024;
    private boolean truncated;

    /**
     * 初始化当前操作的字段排除集合。
     *
     * @param excludeFields 接口显式排除的字段名
     */
    OperationLogRequestSnapshot(String[] excludeFields) {
        excludedNames = Set.copyOf(List.of(excludeFields));
    }

    /**
     * 生成请求摘要，单个请求对象沿用直接展示业务字段的格式。
     *
     * @param method 控制器方法
     * @param arguments 绑定后的请求参数
     * @return 请求摘要 JSON，无可记录字段时为空
     */
    String capture(Method method, Object[] arguments) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        Parameter[] declarations = method.getParameters();
        for (int index = 0; index < declarations.length; index++) {
            Parameter parameter = declarations[index];
            if (isExcludedType(parameter.getType()) || parameter.isAnnotationPresent(RequestHeader.class)
                    || parameter.isAnnotationPresent(CookieValue.class)) {
                continue;
            }
            String name = parameter.getName();
            RequestParam query = parameter.getAnnotation(RequestParam.class);
            PathVariable path = parameter.getAnnotation(PathVariable.class);
            RequestPart part = parameter.getAnnotation(RequestPart.class);
            if (query != null) {
                name = StringUtils.firstNonBlank(query.name(), query.value(), name);
            } else if (path != null) {
                name = StringUtils.firstNonBlank(path.name(), path.value(), name);
            } else if (part != null) {
                name = StringUtils.firstNonBlank(part.name(), part.value(), name);
            }
            if (!excludedNames.contains(parameter.getName())) {
                addField(parameters, name, arguments[index], 0);
            }
        }
        // 导出接口的 ServletResponse 已被排除，查询 DTO 与普通请求体使用相同的展示结构。
        Map<?, ?> fields = parameters;
        if (parameters.size() == 1 && parameters.values().iterator().next() instanceof Map<?, ?> object) {
            fields = object;
        }
        if (truncated) {
            Map<Object, Object> summary = new LinkedHashMap<>(fields);
            summary.put("$truncated", true);
            return JsonUtils.toJson(summary);
        }
        return fields.isEmpty() ? null : JsonUtils.toJson(fields);
    }

    /**
     * 采集一个具名字段，保留批量操作的实际条数。
     *
     * @param target 当前层的字段集合
     * @param name 请求字段名
     * @param value 绑定值
     * @param depth 当前嵌套层数
     */
    private void addField(Map<String, Object> target, String name, Object value, int depth) {
        if (excludedNames.contains(name) || !reserve(6L * name.length() + 4)) {
            return;
        }
        Object captured = captureValue(value, depth);
        if (captured != OMITTED) {
            target.put(name, captured);
            if (value instanceof Collection<?> items) {
                String countName = name + "Count";
                if (!excludedNames.contains(countName) && reserve(6L * countName.length() + 16)) {
                    target.putIfAbsent(countName, items.size());
                }
            }
        }
    }

    /**
     * 采集各层可记录的业务值，并限制长文本、集合及递归深度。
     *
     * @param value 待采集的业务值
     * @param depth 当前嵌套层数
     * @return 可序列化的业务值或省略标记
     */
    private Object captureValue(Object value, int depth) {
        if (value != null && isExcludedType(value.getClass())) {
            return OMITTED;
        }
        if (depth > MAX_DEPTH || !reserve(8)) {
            truncated = true;
            return OMITTED;
        }
        if (value == null) {
            return null;
        }
        Class<?> type = value.getClass();
        if (BeanUtils.isSimpleValueType(type)) {
            Object scalar = value;
            if (value instanceof String text && text.length() > MAX_STRING_LENGTH) {
                scalar = StringUtils.abbreviate(text, MAX_STRING_LENGTH);
                truncated = true;
            }
            return reserve(JsonUtils.toBytes(scalar).length) ? scalar : OMITTED;
        }
        if (value instanceof Collection<?> items) {
            return captureItems(items.stream(), items.size(), depth);
        }
        if (type.isArray()) {
            int size = Array.getLength(value);
            return captureItems(IntStream.range(0, size).mapToObj(index -> Array.get(value, index)), size, depth);
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!reserve(1)) {
                    break;
                }
                if (entry.getKey() instanceof String name) {
                    addField(fields, name, entry.getValue(), depth + 1);
                } else if (entry.getKey() instanceof Number || entry.getKey() instanceof Enum<?>) {
                    addField(fields, entry.getKey().toString(), entry.getValue(), depth + 1);
                }
            }
        } else {
            BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(value);
            for (PropertyDescriptor property : bean.getPropertyDescriptors()) {
                String name = property.getName();
                // 构造器绑定的只读字段也属于请求数据，纯校验 getter 没有对应字段。
                if (property.getReadMethod() == null || isExcludedType(property.getPropertyType())
                        || excludedNames.contains(name)
                        || (property.getWriteMethod() == null && ReflectionUtils.findField(type, name) == null)) {
                    continue;
                }
                TypeDescriptor descriptor = bean.getPropertyTypeDescriptor(name);
                JsonIgnore ignore = descriptor.getAnnotation(JsonIgnore.class);
                JsonProperty binding = descriptor.getAnnotation(JsonProperty.class);
                // 忽略属性和仅供响应读取的属性不参与 JSON 入参绑定。
                if ((ignore != null && ignore.value()) || (binding != null && binding.access() == JsonProperty.Access.READ_ONLY)) {
                    continue;
                }
                String fieldName = binding == null ? name : StringUtils.defaultIfEmpty(binding.value(), name);
                if (!excludedNames.contains(fieldName)) {
                    addField(fields, fieldName, bean.getPropertyValue(name), depth + 1);
                }
            }
        }
        return fields;
    }

    /**
     * 采集批量请求中的前若干项，元素与普通请求字段使用相同的过滤规则。
     *
     * @param items 集合或数组元素
     * @param size 实际条数
     * @param depth 当前嵌套层数
     * @return 已过滤的元素
     */
    private List<Object> captureItems(Stream<?> items, int size, int depth) {
        truncated |= size > MAX_ITEMS;
        return items.limit(MAX_ITEMS).map(item -> captureValue(item, depth + 1))
                .filter(item -> item != OMITTED).toList();
    }

    /**
     * 为 JSON 字段及值预留字节，避免超大请求导致整条操作日志写入失败。
     *
     * @param bytes 此项的字节数或转义后的上界
     * @return 摘要是否有足够空间
     */
    private boolean reserve(long bytes) {
        if (bytes > remainingBytes) {
            truncated = true;
            return false;
        }
        remainingBytes -= (int) bytes;
        return true;
    }

    /**
     * 判断值是否为协议对象、文件或二进制内容。
     *
     * @param type 参数或属性的类型
     * @return 是否跳过此类型
     */
    private static boolean isExcludedType(Class<?> type) {
        return ServletRequest.class.isAssignableFrom(type) || ServletResponse.class.isAssignableFrom(type)
                || Part.class.isAssignableFrom(type) || InputStreamSource.class.isAssignableFrom(type)
                || InputStream.class.isAssignableFrom(type) || OutputStream.class.isAssignableFrom(type)
                || Reader.class.isAssignableFrom(type) || Writer.class.isAssignableFrom(type)
                || Errors.class.isAssignableFrom(type) || type == byte[].class || type == Byte[].class
                || type == char[].class || type == Character[].class;
    }
}
