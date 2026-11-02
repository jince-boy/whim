package com.whim.json.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 统一清理 Jackson 字符串反序列化结果的首尾空白。
 */
public class TrimStringDeserializer extends StringDeserializer {

    /**
     * 读取字符串并去除首尾空白，保留 null 和字符串内部的空白。
     *
     * @param parser JSON 解析器
     * @param context 反序列化上下文
     * @return 清理后的字符串
     */
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) {
        String value = super.deserialize(parser, context);
        return value == null ? null : value.strip();
    }
}
