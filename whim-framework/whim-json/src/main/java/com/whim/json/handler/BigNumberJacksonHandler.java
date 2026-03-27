package com.whim.json.handler;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 大数字 Jackson 处理器
 */
public final class BigNumberJacksonHandler {

    /**
     * 构建大数字处理模块
     *
     * @return 大数字处理模块
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public JacksonModule getModule() {
        var module = new SimpleModule("whim-big-number-jackson-module");
        var longSerializer = new LongValueSerializer();
        module.addSerializer(Long.class, longSerializer);
        module.addSerializer((Class) Long.TYPE, (ValueSerializer) longSerializer);
        module.addSerializer(BigInteger.class, new BigIntegerValueSerializer());
        module.addSerializer(BigDecimal.class, new BigDecimalValueSerializer());
        return module;
    }

    /**
     * Long 序列化器
     */
    private static final class LongValueSerializer extends ValueSerializer<Long> {

        /**
         * 将 Long 序列化为字符串
         *
         * @param value   数值
         * @param jgen    JSON 生成器
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
     * BigInteger 序列化器
     */
    private static final class BigIntegerValueSerializer extends ValueSerializer<BigInteger> {

        /**
         * 将 BigInteger 序列化为字符串
         *
         * @param value   数值
         * @param jgen    JSON 生成器
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
     * BigDecimal 序列化器
     */
    private static final class BigDecimalValueSerializer extends ValueSerializer<BigDecimal> {

        /**
         * 将 BigDecimal 序列化为字符串
         *
         * @param value   数值
         * @param jgen    JSON 生成器
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
