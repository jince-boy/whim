package com.whim.web.deserializer;

import com.whim.web.annotation.Xss;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.BeanDeserializerBuilder;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/27
 * @description XSS 反序列化修饰器，为标注字段注入安全清洗逻辑。
 */
public final class XssDeserializerModifier extends ValueDeserializerModifier {

    /**
     * 更新 Bean 反序列化构建器。
     *
     * @param config          反序列化配置
     * @param beanDescription Bean 描述
     * @param builder         Bean 反序列化构建器
     * @return 更新后的 Bean 反序列化构建器
     */
    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,BeanDescription.Supplier beanDescription,BeanDeserializerBuilder builder) {
        List<SettableBeanProperty> replacements = new ArrayList<>();
        var properties = builder.getProperties();
        while (properties.hasNext()) {
            var property = properties.next();
            var annotation = findXssAnnotation(property);
            if (annotation == null || !String.class.equals(property.getType().getRawClass())) {
                continue;
            }
            replacements.add(property.withValueDeserializer(new XssValueDeserializer(annotation, property.getValueDeserializer())));
        }
        replacements.forEach(property -> builder.addOrReplaceProperty(property, true));
        return builder;
    }

    /**
     * 查找字段上的 XSS 注解。
     *
     * @param property 字段属性定义
     * @return XSS 注解
     */
    private Xss findXssAnnotation(SettableBeanProperty property) {
        if (property == null) {
            return null;
        }
        return property.getAnnotation(Xss.class);
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description XSS 字段值反序列化器，对标注字段进行安全清洗。
     */
    private static final class XssValueDeserializer extends StdScalarDeserializer<String> {
        private final Xss xss;
        private final ValueDeserializer<?> delegate;

        /**
         * 创建 XSS 字段值反序列化器。
         *
         * @param xss      XSS 注解
         * @param delegate 原始字段反序列化器
         */
        private XssValueDeserializer(Xss xss, ValueDeserializer<?> delegate) {
            super(String.class);
            this.xss = Objects.requireNonNull(xss, "参数[xss]不能为空");
            this.delegate = delegate;
        }

        /**
         * 反序列化并清洗字段值。
         *
         * @param jsonParser JSON 解析器
         * @param context    反序列化上下文
         * @return 清洗后的字段值
         * @throws JacksonException Jackson 处理异常
         */
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            var rawValue = deserializeRawValue(jsonParser, context);
            if (rawValue == null) {
                return null;
            }
            return xss.policy().sanitize(rawValue);
        }

        /**
         * 读取原始字符串值。
         *
         * @param jsonParser JSON 解析器
         * @param context    反序列化上下文
         * @return 原始字符串值
         * @throws JacksonException Jackson 处理异常
         */
        private String deserializeRawValue(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            if (delegate != null && !(delegate instanceof XssValueDeserializer)) {
                return (String) delegate.deserialize(jsonParser, context);
            }
            return jsonParser.getValueAsString();
        }
    }
}
