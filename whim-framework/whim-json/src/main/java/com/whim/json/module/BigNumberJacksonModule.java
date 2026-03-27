package com.whim.json.module;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Jince
 * @date 2026/03/28
 * @description 大数字 Jackson 模块，负责注册避免前端精度丢失的序列化能力。
 */
public final class BigNumberJacksonModule extends SimpleModule {

    /**
     * 创建大数字 Jackson 模块。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public BigNumberJacksonModule() {
        super("whim-big-number-jackson-module");
        var longSerializer = new LongValueSerializer();
        addSerializer(Long.class, longSerializer);
        addSerializer((Class) Long.TYPE, (ValueSerializer) longSerializer);
        addSerializer(BigInteger.class, new BigIntegerValueSerializer());
        addSerializer(BigDecimal.class, new BigDecimalValueSerializer());
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Long 序列化器。
     */
    private static final class LongValueSerializer extends ValueSerializer<Long> {

        /**
         * 将 Long 序列化为字符串。
         *
         * @param value 数值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(Long value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            jgen.writeString(value.toString());
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description BigInteger 序列化器。
     */
    private static final class BigIntegerValueSerializer extends ValueSerializer<BigInteger> {

        /**
         * 将 BigInteger 序列化为字符串。
         *
         * @param value 数值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(BigInteger value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            jgen.writeString(value.toString());
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description BigDecimal 序列化器。
     */
    private static final class BigDecimalValueSerializer extends ValueSerializer<BigDecimal> {

        /**
         * 将 BigDecimal 序列化为字符串。
         *
         * @param value 数值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(BigDecimal value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            jgen.writeString(value.toPlainString());
        }
    }
}
