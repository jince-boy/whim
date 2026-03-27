package com.whim.json.serializer;

import com.whim.json.annotation.Desensitize;
import com.whim.json.spi.DesensitizationAccessEvaluator;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 脱敏序列化修饰器，为标注了脱敏注解的字段动态注入脱敏序列化器。
 */
public final class DesensitizeSerializerModifier extends ValueSerializerModifier {
    private final DesensitizationAccessEvaluator accessEvaluator;

    /**
     * 创建脱敏序列化修饰器。
     *
     * @param accessEvaluator 脱敏访问评估器
     */
    public DesensitizeSerializerModifier(DesensitizationAccessEvaluator accessEvaluator) {
        this.accessEvaluator = Objects.requireNonNull(accessEvaluator, "accessEvaluator must not be null");
    }

    /**
     * 修改 Bean 属性写出器列表。
     *
     * @param config 序列化配置
     * @param beanDescription Bean 描述
     * @param beanProperties 属性写出器列表
     * @return 修改后的属性写出器列表
     */
    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config,
            BeanDescription.Supplier beanDescription,
            List<BeanPropertyWriter> beanProperties
    ) {
        for (var writer : beanProperties) {
            var annotation = findDesensitizeAnnotation(writer.getMember());
            if (annotation == null) {
                continue;
            }
            validateStringProperty(writer);
            writer.assignSerializer(new DesensitizeValueSerializer(annotation, accessEvaluator));
        }
        return beanProperties;
    }

    /**
     * 查找字段上的脱敏注解。
     *
     * @param member 字段成员信息
     * @return 脱敏注解
     */
    private Desensitize findDesensitizeAnnotation(AnnotatedMember member) {
        if (member == null) {
            return null;
        }
        return member.getAnnotation(Desensitize.class);
    }

    /**
     * 校验脱敏注解仅用于字符串字段。
     *
     * @param writer 字段写出器
     */
    private void validateStringProperty(BeanPropertyWriter writer) {
        if (String.class.equals(writer.getType().getRawClass())) {
            return;
        }
        throw new IllegalStateException("@Desensitize only supports String properties: " + writer.getFullName());
    }

    /**
     * @author Jince
     * @date 2026/03/27
     * @description 脱敏字段值序列化器。
     */
    private static final class DesensitizeValueSerializer extends ValueSerializer<Object> {
        private final Desensitize desensitize;
        private final DesensitizationAccessEvaluator accessEvaluator;

        /**
         * 创建脱敏字段值序列化器。
         *
         * @param desensitize 脱敏注解
         * @param accessEvaluator 脱敏访问评估器
         */
        private DesensitizeValueSerializer(Desensitize desensitize, DesensitizationAccessEvaluator accessEvaluator) {
            this.desensitize = Objects.requireNonNull(desensitize, "desensitize must not be null");
            this.accessEvaluator = Objects.requireNonNull(accessEvaluator, "accessEvaluator must not be null");
        }

        /**
         * 序列化字段值。
         *
         * @param value 字段值
         * @param jsonGenerator JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(Object value, JsonGenerator jsonGenerator, SerializationContext context) {
            if (value == null) {
                jsonGenerator.writeNull();
                return;
            }

            var text = String.valueOf(value);
            if (canViewPlainValue()) {
                jsonGenerator.writeString(text);
                return;
            }

            jsonGenerator.writeString(desensitize.type().mask(
                    text,
                    desensitize.prefixKeep(),
                    desensitize.suffixKeep(),
                    desensitize.maskChar()
            ));
        }

        /**
         * 判断当前上下文是否允许输出原始值。
         *
         * @return 为 true 表示允许输出原始值
         */
        private boolean canViewPlainValue() {
            var roles = desensitize.roles();
            var authorities = desensitize.authorities();
            if (roles.length == 0 && authorities.length == 0) {
                return false;
            }
            return accessEvaluator.canViewPlainValue(roles, authorities, desensitize.requireAll());
        }
    }
}
