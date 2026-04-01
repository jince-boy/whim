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
 * @description 大数字 Jackson 模块，按 JavaScript Number 的安全边界决定输出为数字还是字符串。
 */
public final class BigNumberJacksonModule extends SimpleModule {
    /**
     * JavaScript Number 可精确表示的最大整数值。
     */
    private static final long JS_MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    /**
     * JavaScript Number 可精确表示的最大整数边界。
     */
    private static final BigInteger JS_MAX_SAFE_INTEGER_BIGINT = BigInteger.valueOf(JS_MAX_SAFE_INTEGER);

    /**
     * JavaScript Number 可精确表示的最小整数边界。
     */
    private static final BigInteger JS_MIN_SAFE_INTEGER_BIGINT = BigInteger.valueOf(-JS_MAX_SAFE_INTEGER);

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

    private static final class LongValueSerializer extends ValueSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) { jgen.writeNull(); return; }
            if (isSafeInteger(value)) {
                jgen.writeNumber(value);
                return;
            }
            jgen.writeString(value.toString());
        }
    }

    private static final class BigIntegerValueSerializer extends ValueSerializer<BigInteger> {
        @Override
        public void serialize(BigInteger value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) { jgen.writeNull(); return; }
            if (isSafeInteger(value)) {
                jgen.writeNumber(value);
                return;
            }
            jgen.writeString(value.toString());
        }
    }

    private static final class BigDecimalValueSerializer extends ValueSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) { jgen.writeNull(); return; }
            if (isSafeDecimal(value)) {
                jgen.writeNumber(value);
                return;
            }
            jgen.writeString(value.toPlainString());
        }
    }

    /**
     * 判断 long 是否落在 JavaScript 安全整数范围内。
     */
    private static boolean isSafeInteger(long value) {
        return value >= -JS_MAX_SAFE_INTEGER && value <= JS_MAX_SAFE_INTEGER;
    }

    /**
     * 判断 BigInteger 是否落在 JavaScript 安全整数范围内。
     */
    private static boolean isSafeInteger(BigInteger value) {
        return value.compareTo(JS_MIN_SAFE_INTEGER_BIGINT) >= 0
                && value.compareTo(JS_MAX_SAFE_INTEGER_BIGINT) <= 0;
    }

    /**
     * 判断 BigDecimal 是否可被 JavaScript Number 精确表示。
     */
    private static boolean isSafeDecimal(BigDecimal value) {
        var normalized = value.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            return isSafeInteger(normalized.toBigIntegerExact());
        }
        var doubleValue = normalized.doubleValue();
        if (!Double.isFinite(doubleValue)) {
            return false;
        }
        return normalized.compareTo(BigDecimal.valueOf(doubleValue).stripTrailingZeros()) == 0;
    }
}
