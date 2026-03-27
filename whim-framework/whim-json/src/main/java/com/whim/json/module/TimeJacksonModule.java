package com.whim.json.module;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Jince
 * @date 2026/03/28
 * @description 时间 Jackson 模块，负责注册时间类型的序列化与反序列化能力。
 */
public final class TimeJacksonModule extends SimpleModule {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

    /**
     * 创建时间 Jackson 模块。
     */
    public TimeJacksonModule() {
        super("whim-time-jackson-module");
        addSerializer(LocalDateTime.class, new LocalDateTimeValueSerializer());
        addDeserializer(LocalDateTime.class, new LocalDateTimeValueDeserializer());
        addSerializer(Date.class, new DateValueSerializer());
        addDeserializer(Date.class, new DateValueDeserializer());
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description 本地日期时间序列化器。
     */
    private static final class LocalDateTimeValueSerializer extends ValueSerializer<LocalDateTime> {

        /**
         * 序列化本地日期时间。
         *
         * @param value 日期时间值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(LocalDateTime value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            jgen.writeString(value.format(DATE_TIME_FORMATTER));
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description 本地日期时间反序列化器。
     */
    private static final class LocalDateTimeValueDeserializer extends ValueDeserializer<LocalDateTime> {

        /**
         * 反序列化本地日期时间。
         *
         * @param parser JSON 解析器
         * @param context 反序列化上下文
         * @return 本地日期时间
         */
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) {
            var text = parser.getString();
            if (text == null || text.isBlank()) {
                return null;
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Date 序列化器。
     */
    private static final class DateValueSerializer extends ValueSerializer<Date> {

        /**
         * 序列化 Date。
         *
         * @param value 日期值
         * @param jgen JSON 生成器
         * @param context 序列化上下文
         */
        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
                return;
            }
            var localDateTime = LocalDateTime.ofInstant(value.toInstant(), SYSTEM_ZONE_ID);
            jgen.writeString(localDateTime.format(DATE_TIME_FORMATTER));
        }
    }

    /**
     * @author Jince
     * @date 2026/03/28
     * @description Date 反序列化器。
     */
    private static final class DateValueDeserializer extends ValueDeserializer<Date> {

        /**
         * 反序列化 Date。
         *
         * @param parser JSON 解析器
         * @param context 反序列化上下文
         * @return 日期值
         */
        @Override
        public Date deserialize(JsonParser parser, DeserializationContext context) {
            var text = parser.getString();
            if (text == null || text.isBlank()) {
                return null;
            }
            var localDateTime = LocalDateTime.parse(text, DATE_TIME_FORMATTER);
            return Date.from(localDateTime.atZone(SYSTEM_ZONE_ID).toInstant());
        }
    }
}
