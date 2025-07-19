package com.whim.json.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * @author jince
 * @date 2025/6/17 21:17
 * @description 自定义 Jackson 序列化器，用于处理大数字（Long/BigInteger）的 JSON 序列化，
 * 防止 JavaScript 因精度丢失导致的问题。
 * <p>
 * <b>核心逻辑：</b>
 * 当数字值在安全范围内（-9007199254740991 ~ 9007199254740991）时，按普通数字序列化；
 * 超出安全范围时，序列化为字符串格式。
 * </p>
 *
 * <p>
 * <b>背景说明：</b>
 * JavaScript 的 Number 类型最大安全整数为 2^53 - 1（9007199254740991），
 * 超过此值的整数会被截断导致精度丢失。此序列化器确保大数字始终以字符串形式传输。
 * </p>
 */
@JacksonStdImpl // 标识为 Jackson 标准实现
public class BigNumberSerializer extends NumberSerializer {

    /**
     * JavaScript 最大安全整数（2^53 - 1）
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    /**
     * JavaScript 最小安全整数（-(2^53 - 1)）
     */
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 单例实例（线程安全）
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    /**
     * 构造方法
     *
     * @param rawType 处理的数字类型（如 Long.class/BigInteger.class）
     */
    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    /**
     * 序列化逻辑实现
     *
     * @param value    待序列化的数字值
     * @param gen      JSON 生成器
     * @param provider 序列化上下文
     * @throws IOException 序列化异常
     */
    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 在安全范围内：按普通数字序列化
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            super.serialize(value, gen, provider);
        }
        // 超出安全范围：序列化为字符串
        else {
            gen.writeString(value.toString());
        }
    }
}
